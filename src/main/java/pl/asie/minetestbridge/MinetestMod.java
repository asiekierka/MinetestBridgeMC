package pl.asie.minetestbridge;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

public class MinetestMod {
    private final File dir;
    private final String name;
    private final Collection<String> dependencies;

    public MinetestMod(File dir) {
        this.dir = dir;
        this.name = dir.getName();

        File dependsFile = new File(dir, "depends.txt");
        if (dependsFile.exists()) {
            ImmutableSet.Builder<String> depBuilder = new ImmutableSet.Builder<>();

            try {
                for (String s : Files.readLines(dependsFile, Charsets.UTF_8)) {
                    s = s.trim();
                    if (!s.isEmpty()) {
                        depBuilder.add(s);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            this.dependencies = depBuilder.build();
        } else {
            this.dependencies = Collections.emptySet();
        }
    }

    public File getDir() {
        return dir;
    }

    public String getName() {
        return name;
    }

    public Collection<String> getDependencies() {
        return dependencies;
    }

    public void preInit() {
        MinetestBridge.globals.get("dofile").call(LuaValue.valueOf(new File(dir, "init.lua").getAbsolutePath()));
    }
}
