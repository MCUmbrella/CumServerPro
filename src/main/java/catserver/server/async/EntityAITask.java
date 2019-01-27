package catserver.server.async;

import net.minecraft.entity.ai.EntityAITasks;

public class EntityAITask extends AbstractEntityTask{
    private final EntityAITasks tasks;

    public EntityAITask(EntityAITasks tasks) {
        this.tasks = tasks;
    }


    @Override
    public void run() {
        tasks.onUpdateTasks0();
    }
}
