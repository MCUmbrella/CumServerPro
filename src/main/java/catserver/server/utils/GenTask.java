package catserver.server.utils;

import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.IChunkGenerator;

public class GenTask {
    public final ChunkProviderServer chunkProviderServer;
    public final IChunkGenerator generator;
    public final int x;
    public final int z;
    public final long posHash;
    public PlayerChunkMapEntry entry;

    public GenTask(ChunkProviderServer chunkProviderServer, IChunkGenerator generator, int x, int z, long posHash, PlayerChunkMapEntry entry) {
        this.chunkProviderServer = chunkProviderServer;
        this.generator = generator;
        this.x = x;
        this.z = z;
        this.posHash = posHash;
        this.entry = entry;
    }
}
