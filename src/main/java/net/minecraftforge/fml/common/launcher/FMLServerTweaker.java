/*
 * Minecraft Forge
 * Copyright (c) 2016.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.minecraftforge.fml.common.launcher;

import java.io.File;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;

import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;

public class FMLServerTweaker extends FMLTweaker {

    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile)
    {
        super.acceptOptions(args, gameDir, assetsDir, profile);

        if (System.getProperty("log4j.configurationFile") == null)
        {
            System.setProperty("log4j.configurationFile", "log4j2.xml");
            ((LoggerContext) LogManager.getContext(false)).reconfigure();
        }
    }

    @Override
    public String getLaunchTarget()
    {
        return "net.minecraft.server.MinecraftServer";
    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader)
    {
        // The log4j2 queue is excluded so it is correctly visible from the obfuscated
        // and deobfuscated parts of the code. Without, the UI won't show anything
        classLoader.addClassLoaderExclusion("com.mojang.util.QueueLogAppender");

        classLoader.addClassLoaderExclusion("jline.");
        classLoader.addClassLoaderExclusion("org.fusesource.");
        classLoader.addClassLoaderExclusion("net.minecraftforge.server.console.log4j.TerminalConsoleAppender");

        try {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader("net.minecraftforge.fml.relauncher.ServerLaunchWrapper");
            classReader.accept(classNode, 0);
            classNode.fields.add(new FieldNode(ACC_PUBLIC + ACC_STATIC, "tickTime", "J", null, null));
            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            classNode.accept(classWriter);
            ClassLoader cl = classLoader;
            Method method = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class, ProtectionDomain.class);
            method.setAccessible(true);
            byte[] bytes = classWriter.toByteArray();
            method.invoke(cl, "net.minecraftforge.fml.relauncher.ServerLaunchWrapper", bytes, 0, bytes.length, null);
        }catch (Throwable throwable) {}

        FMLLaunchHandler.configureForServerLaunch(classLoader, this);
        FMLLaunchHandler.appendCoreMods();
    }
}
