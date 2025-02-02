package net.mca.client.gui.immersiveLibrary;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import net.mca.client.gui.immersiveLibrary.responses.ContentResponse;
import net.mca.client.gui.immersiveLibrary.responses.Response;
import net.mca.client.gui.immersiveLibrary.types.LiteContent;
import net.mca.client.resources.SkinMeta;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static net.mca.client.gui.immersiveLibrary.Api.request;

public class SkinCache {
    private static final Identifier DEFAULT_SKIN = new Identifier("textures/entity/steve.png");

    private static final Gson gson = new Gson();

    static final Set<Integer> requested = new HashSet<>();
    static final Map<Integer, Identifier> textureIdentifiers = new HashMap<>();
    static final Map<Integer, NativeImage> images = new HashMap<Integer, NativeImage>();
    static final Map<Integer, SkinMeta> metaCache = new HashMap<>();

    private static File getFile(String key) {
        //noinspection ResultOfMethodCallIgnored
        new File("./immersive_library/").mkdirs();

        return new File("./immersive_library/" + key);
    }

    private static void write(String file, String content) {
        try {
            FileUtils.writeStringToFile(getFile(file), content, Charset.defaultCharset(), false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void write(String file, byte[] content) {
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(getFile(file)))) {
            out.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String read(String file) {
        try {
            return FileUtils.readFileToString(getFile(file), Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void clearRequested() {
        requested.clear();
    }

    public static void enforceSync(int contentid) {
        //noinspection ResultOfMethodCallIgnored
        getFile(contentid + ".version").delete();
    }

    public static void sync(LiteContent content) {
        sync(content.contentid(), content.version());
    }

    public static void sync(int contentid, int currentVersion) {
        if (!requested.contains(contentid)) {
            requested.add(contentid);

            int version = -1;
            try {
                String s = FileUtils.readFileToString(getFile(contentid + ".version"), Charset.defaultCharset());
                version = Integer.parseInt(s);
            } catch (Exception ignored) {
            }

            if (currentVersion == version) {
                loadResources(contentid);
            } else if (currentVersion >= 0) {
                //noinspection ResultOfMethodCallIgnored
                getFile(contentid + ".version").delete();

                CompletableFuture.runAsync(() -> {
                    Response response = request(Api.HttpMethod.GET, ContentResponse.class, "content/mca/%s".formatted(contentid));
                    if (response instanceof ContentResponse contentResponse) {
                        write(contentid + ".png", Base64.getDecoder().decode(contentResponse.content().data()));
                        write(contentid + ".json", contentResponse.content().meta());
                        write(contentid + ".version", Integer.toString(contentResponse.content().version()));
                        loadResources(contentid);
                    }
                });
            }
        }
    }

    private static void loadResources(int contentid) {
        // Load meta
        try {
            String json = read(contentid + ".json");
            SkinMeta meta = gson.fromJson(json, SkinMeta.class);
            metaCache.put(contentid, meta);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            enforceSync(contentid);
        }

        // Load texture
        try (FileInputStream stream = new FileInputStream(getFile(contentid + ".png").getPath())) {
            // Load new
            NativeImage image = NativeImage.read(stream);
            Identifier identifier = new Identifier("immersive_library", String.valueOf(contentid));
            MinecraftClient.getInstance().getTextureManager().registerTexture(identifier, new NativeImageBackedTexture(image));
            textureIdentifiers.put(contentid, identifier);
            images.put(contentid, image);
        } catch (IOException e) {
            e.printStackTrace();
            enforceSync(contentid);
        }
    }

    public static Optional<SkinMeta> getMeta(LiteContent content) {
        sync(content);
        return Optional.ofNullable(metaCache.get(content.contentid()));
    }

    public static Optional<NativeImage> getImage(LiteContent content) {
        sync(content);
        return Optional.ofNullable(images.get(content.contentid()));
    }

    public static Identifier getTextureIdentifier(LiteContent content) {
        sync(content);
        return textureIdentifiers.getOrDefault(content.contentid(), DEFAULT_SKIN);
    }

    public static Identifier getTextureIdentifier(int contentid) {
        sync(contentid, -2);
        return textureIdentifiers.getOrDefault(contentid, DEFAULT_SKIN);
    }
}
