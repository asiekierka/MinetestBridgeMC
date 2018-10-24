package pl.asie.minetestbridge.client;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.luaj.vm2.LuaValue;
import pl.asie.minetestbridge.MinetestBridge;
import pl.asie.minetestbridge.MinetestMod;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class TextureMinetestSprite extends TextureAtlasSpriteCustom {
    private final File dir;
    private final Collection<MinetestMod> mods;
    private final String path;
    private final LuaValue obj;

    protected TextureMinetestSprite(String spriteName, File dir, Collection<MinetestMod> mods, String path, LuaValue obj) {
        super(spriteName);
        this.dir = dir;
        this.mods = mods;
        this.path = path;
        this.obj = obj;
    }

    @Override
    public boolean hasCustomLoader(IResourceManager manager, ResourceLocation location) {
        return true;
    }

    @Override
    public boolean load(IResourceManager manager, ResourceLocation loc, Function<ResourceLocation, TextureAtlasSprite> getter) {
        Function<BufferedImage, BufferedImage> function = (i) -> new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);

        for (String s : path.split("\\^")) {
            if (s.isEmpty()) {
                continue;
            }

            Function<BufferedImage, BufferedImage> func = null;

            if (s.startsWith("[")) {
                String cmd = s.substring(1);
                String cmdLower = cmd.toLowerCase();

                if (cmdLower.startsWith("makealpha")) {
                    String[] cols = cmdLower.split(":")[1].split(",");
                    int color = (Integer.parseInt(cols[0]) << 16) | (Integer.parseInt(cols[1]) << 8) | (Integer.parseInt(cols[2]));

                    func = (src) -> {
                        for (int y = 0; y < src.getHeight(); y++) {
                            for (int x = 0; x < src.getWidth(); x++) {
                                int colSrc = src.getRGB(x, y);
                                if ((colSrc & 0xFFFFFF) == color) {
                                    src.setRGB(x, y, 0);
                                }
                            }
                        }

                        return src;
                    };
                } else if (cmdLower.startsWith("transform")) {
                    func = (src) -> {
                        // transforms, eh
                        double deg90 = Math.PI / 2;
                        AffineTransform transform = new AffineTransform();
                        int i = 9;
                        while (i < cmdLower.length()) {
                            int tval = -1;

                            char c = cmdLower.charAt(i++);
                            if (c >= '0' && c <= '7') {
                                tval = (c - '0');
                            } else if (c == 'f') {
                                c = cmdLower.charAt(i++);
                                if (c == 'x') {
                                    tval = 4;
                                } else if (c == 'y') {
                                    tval = 6;
                                }
                            } else if (c == 'r') {
                                // TODO: actually read the value
                                c = cmdLower.charAt(i++);
                                if (c == '9') {
                                    tval = 1;
                                    i++;
                                } else if (c == '1') {
                                    tval = 2;
                                    i += 2;
                                } else if (c == '2') {
                                    tval = 3;
                                    i += 2;
                                }
                            }

                            switch (tval) {
                                default:
                                    MinetestBridge.logger.warn("Unsupported texture component: " + s);
                                    i = cmdLower.length();
                                    break;
                                case 0:
                                    break;
                                case 1:
                                    transform.rotate(deg90);
                                    break;
                                case 2:
                                    transform.rotate(deg90 * 2);
                                    break;
                                case 3:
                                    transform.rotate(deg90 * 3);
                                    break;
                                case 4:
                                    transform.scale(-1, 1);
                                    transform.translate(-src.getWidth(), 0);
                                    break;
                                case 5:
                                    transform.scale(-1, 1);
                                    transform.translate(-src.getWidth(), 0);
                                    transform.rotate(deg90);
                                    break;
                                case 6:
                                    transform.scale(1, -1);
                                    transform.translate(0, -src.getHeight());
                                    break;
                                case 7:
                                    transform.scale(1, -1);
                                    transform.translate(0, -src.getHeight());
                                    transform.rotate(deg90);
                                    break;
                            }
                        }

                        BufferedImage dst = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
                        new AffineTransformOp(transform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR).filter(src, dst);
                        return dst;
                    };
                } else {
                    MinetestBridge.logger.warn("Unsupported texture component: " + s);
                }
            } else {
                func = (i) -> {
                    File f = null;
                    for (MinetestMod mod : mods) {
                        File ff = new File(dir, mod.getName() + "/textures/" + s);
                        if (ff.exists()) {
                            f = ff;
                            break;
                        }
                    }

                    if (f == null) {
                        MinetestBridge.logger.warn("Texture not found: " + s);
                        return i;
                    }

                    try {
                        BufferedImage img = TextureUtil.readBufferedImage(new FileInputStream(f));
                        BufferedImage target = i;

                        if (img.getWidth() != i.getWidth() || img.getHeight() != i.getHeight()) {
                            target = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
                            BufferedImage scaledImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);;

                            AffineTransform atImg = new AffineTransform();
                            AffineTransform atI = new AffineTransform();
                            atImg.scale((double) target.getWidth() / img.getWidth(), (double) target.getHeight() / img.getHeight());
                            atI.scale((double) target.getWidth() / i.getWidth(), (double) target.getHeight() / i.getHeight());
                            new AffineTransformOp(atImg, AffineTransformOp.TYPE_NEAREST_NEIGHBOR).filter(img, scaledImg);
                            new AffineTransformOp(atI, AffineTransformOp.TYPE_NEAREST_NEIGHBOR).filter(i, target);

                            target.getGraphics().drawImage(scaledImg, 0, 0, null);
                        } else {
                            target.getGraphics().drawImage(img, 0, 0, null);
                        }

                        return target;
                    } catch (Exception e) {
                        MinetestBridge.logger.warn("Error drawing texture " + s, e);
                        return i;
                    }
                };
            }

            if (func != null) {
                function = function.andThen(func);
            }
        }

        BufferedImage img = function.apply(null);
        int[] pixels = new int[img.getWidth() * img.getHeight()];
        img.getRGB(0, 0, img.getWidth(), img.getHeight(), pixels, 0, img.getWidth());
        this.addFrameTextureData(img.getWidth(), img.getHeight(), pixels);
        return false;
    }
}
