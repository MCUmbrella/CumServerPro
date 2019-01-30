package catserver.server.async;

import net.minecraft.entity.ai.EntityAITasks;

public class EntityAITask implements EntityTask{
    private final EntityAITasks tasks;

    public EntityAITask(EntityAITasks tasks) {
        this.tasks = tasks;
    }


    @Override
    public void run() {
        tasks.onUpdateTasks0();
    }
}
