package atomicstryker.battletowers.world;

final class TowerShape {
    static final int RADIUS = 6;

    private TowerShape() {}

    static boolean inside(int x, int z) {
        return x * x + z * z <= 32;
    }

    static boolean wall(int x, int z) {
        int d = x * x + z * z;
        return d > 32 && d <= 42;
    }

    static boolean window(int x, int z, int y) {
        if (y != 2 && y != 3) return false;
        return (Math.abs(x) == RADIUS && Math.abs(z) <= 1)
                || (Math.abs(z) == RADIUS && Math.abs(x) <= 1);
    }
}
