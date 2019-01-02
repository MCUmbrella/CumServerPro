package gradle.task;

import lombok.Getter;
import lombok.Setter;
import net.md_5.specialsource.Jar;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ShrinkJarTask extends DefaultTask {

    @Setter
    @InputFile
    private File inputJar;

    @Getter
    @OutputDirectory
    private File classesServer = new File(new File(getProject().getBuildDir(), getName()), "classes_server");

    @TaskAction
    private void doTask() throws IOException {
        try (Jar input = Jar.init(inputJar)) {
            for (String entryName : input.getEntryNames()) {
                if (entryName.endsWith(".class")) {
                    byte[] classBytes = IOUtils.toByteArray(input.getResource(entryName));
                    byte[] serverCls = processClass(classBytes);
                    if (serverCls != null)
                        FileUtils.writeByteArrayToFile(new File(classesServer, entryName), serverCls);
                }
            }
        }
    }

    private byte[] processClass(byte[] input) {
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(input);
        classReader.accept(classNode, 0);
        if (classNode.visibleAnnotations != null)
        classNode.visibleAnnotations.removeIf(annotationNode -> isClientAnnotation(annotationNode));

        classNode.fields.forEach(field -> {
            if (field.visibleAnnotations != null)
            field.visibleAnnotations.removeIf(this::isClientAnnotation);
        });
        classNode.methods.forEach(method -> {
            if (method.visibleAnnotations != null)
            method.visibleAnnotations.removeIf(this::isClientAnnotation);
        });

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(writer);
        return writer.toByteArray();
    }

    private boolean isClientAnnotation(AnnotationNode annotation) {
        if (annotation == null)
            return false;
        if (annotation.desc.equals("Lnet/minecraftforge/fml/relauncher/SideOnly;") && annotation.values != null) {
            for (int x = 0; x < annotation.values.size() - 1; x += 2) {
                Object key = annotation.values.get(x);
                Object value = annotation.values.get(x + 1);
                if (key.equals("value") && value instanceof String[] && ((String[]) value)[1].equals("CLIENT"))
                    return true;
            }
        }
        return false;
    }
}
