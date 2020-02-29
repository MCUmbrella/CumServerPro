package CumServer.server;

public class CumServerLaunch {
    public static void main(String[] args) throws Throwable {
    	System.out.println("  ___           ___\n / __|  _ _ __ / __| ___ _ ___ _____ _ _\n| (_| || | '  \\\\__ \\/ -_) '_\\ V / -_) '_|\n \\___\\_,_|_|_|_|___/\\___|_|  \\_/\\___|_|  is starting!");
        Class.forName("net.minecraftforge.fml.relauncher.ServerLaunchWrapper").getDeclaredMethod("main", String[].class).invoke(null, new Object[] { args });
    }
}
