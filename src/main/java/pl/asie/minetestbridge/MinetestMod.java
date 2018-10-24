/*
 * Copyright (c) 2015, 2016, 2017, 2018 Adrian Siekierka
 *
 * This file is part of MinetestBridge.
 *
 * MinetestBridge is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MinetestBridge is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with MinetestBridge.  If not, see <http://www.gnu.org/licenses/>.
 */

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
