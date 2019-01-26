package catserver.server.threads;

import catserver.server.async.GenTask;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.IChunkGenerator;

import java.util.concurrent.LinkedBlockingQueue;

public class ChunkGenThread extends Thread {
    private final WorldServer worldServer;
    private final LinkedBlockingQueue<GenTask> queue;

    public ChunkGenThread(WorldServer worldServer, LinkedBlockingQueue<GenTask> queue) {
        this.worldServer = worldServer;
        this.queue = queue;
    }

    @Override
    public void run() {
        GenTask thisTask = null;
        while (worldServer != null) {
            try {
                GenTask task = queue.take();
                thisTask = task;
                IChunkGenerator chunkGenerator = task.generator;
                Chunk genChunk = chunkGenerator.generateChunk(task.x, task.z);
                worldServer.getMinecraftServer().processQueue.add(new DefineChunk(task.chunkProviderServer, genChunk, task.posHash, task.generator, task));
            }catch(Throwable throwable) {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Exception generating new chunk");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Chunk to be generated");
                crashreportcategory.addCrashSection("Location", String.format("%d,%d", thisTask.x, thisTask.z));
                crashreportcategory.addCrashSection("Position hash", thisTask.posHash);
                crashreportcategory.addCrashSection("Generator", thisTask.generator);
                throw new ReportedException(crashreport);
            }
        }
    }

    private class DefineChunk implements Runnable {
        private final Chunk chunk;
        private final ChunkProviderServer chunkProviderServer;
        private final long chunkHash;
        private final IChunkGenerator generator;
        private final GenTask genTask;

        DefineChunk(ChunkProviderServer chunkProviderServer, Chunk chunk, long chunkHash, IChunkGenerator generator, GenTask genTask) {
            this.chunkProviderServer = chunkProviderServer;
            this.chunk = chunk;
            this.chunkHash = chunkHash;
            this.generator = generator;
            this.genTask = genTask;
        }

        @Override
        public void run() {
            chunkProviderServer.id2ChunkMap.put(chunkHash, chunk);
            chunk.onLoad();
            genTask.entry.chunk = chunk;
            chunk.populateCB(chunkProviderServer, generator, true);
            genTask.entry.loading = false;
        }
    }
}
