package net.dumbcode.projectnublar.entity.ik.parts;

import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class WorldCollidingSegment extends Segment {
    private Level level;

    public WorldCollidingSegment(Segment.Builder builder) {
        super(builder);
    }

    public void setup(Level level, Vec3 initialPos) {
        this.setLevel(level);
        this.position = initialPos;
    }

    public Level getLevel() {
        return this.level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    /**
     * You need to call {@link #setLevel(Level)} before calling this method!!
     */
    @Override
    public void move(Vec3 position) {
        this.move(position, true);
    }

    public void move(Vec3 position, boolean checkCollision) {
        this.move(position, checkCollision, 0);
    }

    public void move(Vec3 position, boolean checkCollision, double risingAmount) {
        if (this.level == null) {
            throw new IllegalStateException("WorldCollidingSegment has not been setup with a level");
        }

        Vec3 oldPosition = this.getPosition();

        super.move(position);

        if (checkCollision) {
            BlockHitResult collisionPoint = this.level.clip(new ClipContext(
                    oldPosition.add(0, risingAmount, 0),
                    this.getPosition(),
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE,
                    new Arrow(this.level, this.getPosition().x(), this.getPosition().y(), this.getPosition().z())
            ));

            Vec3 direction = collisionPoint.getLocation().subtract(this.getPosition()).normalize();

            super.move(collisionPoint.getLocation().add(direction.scale(0.01)));
        }
    }

    public void move(Vec3 position, Vec3 link) {
        if (this.level == null) {
            throw new IllegalStateException("WorldCollidingSegment has not been setup with a level");
        }

        this.move(position, true);

        Vec3 oldPosition = this.getPosition();

        BlockHitResult blockCollision = this.level.clip(new ClipContext(
                link,
                this.getPosition(),
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                new Arrow(this.level, this.getPosition().x(), this.getPosition().y(), this.getPosition().z())
        ));

        if (blockCollision.getType() == BlockHitResult.Type.MISS) {
            return;
        }

        super.move(oldPosition);
    }
}
