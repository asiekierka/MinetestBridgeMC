package pl.asie.minetestbridge.client;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import pl.asie.minetestbridge.MinetestMod;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class TextureMinetestSprite extends TextureAtlasSpriteCustom {
    private final File dir;
    private final Collection<MinetestMod> mods;
    private final String suffix;

    protected TextureMinetestSprite(String spriteName, File dir, Collection<MinetestMod> mods, String suffix) {
        super(spriteName);
        this.dir = dir;
        this.mods = mods;
        this.suffix = suffix;
    }

    @Override
    public boolean hasCustomLoader(IResourceManager manager, ResourceLocation location) {
        return true;
    }

    @Override
    public boolean load(IResourceManager manager, ResourceLocation loc, Function<ResourceLocation, TextureAtlasSprite> getter) {
        try {
            File f = null;
            for (MinetestMod mod : mods) {
                File ff = new File(dir, mod.getName() + suffix);
                if (ff.exists()) {
                    f = ff;
                    break;
                }
            }

            if (f == null) {
                return true;
            }

            BufferedImage img = TextureUtil.readBufferedImage(new FileInputStream(f));
            int[] pixels = new int[img.getWidth() * img.getHeight()];
            img.getRGB(0, 0, img.getWidth(), img.getHeight(), pixels, 0, img.getWidth());
            this.addFrameTextureData(img.getWidth(), img.getHeight(), pixels);
            return false;
        } catch (IOException e) {
            return true;
        }
    }
}
