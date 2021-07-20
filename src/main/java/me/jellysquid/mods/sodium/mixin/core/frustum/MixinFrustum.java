package me.jellysquid.mods.sodium.mixin.core.frustum;

import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegionVisibility;
import me.jellysquid.mods.sodium.client.util.math.FrustumExtended;
import net.minecraft.client.render.Frustum;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Frustum.class)
public class MixinFrustum implements FrustumExtended {
    private float xF, yF, zF;

    private float nxX, nxY, nxZ, nxW;
    private float pxX, pxY, pxZ, pxW;
    private float nyX, nyY, nyZ, nyW;
    private float pyX, pyY, pyZ, pyW;
    private float nzX, nzY, nzZ, nzW;
    private float pzX, pzY, pzZ, pzW;

    @Inject(method = "setPosition", at = @At("HEAD"))
    private void prePositionUpdate(double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
        this.xF = (float) cameraX;
        this.yF = (float) cameraY;
        this.zF = (float) cameraZ;
    }

    @Inject(method = "transform", at = @At("HEAD"))
    private void transform(Matrix4f mat, int x, int y, int z, int index, CallbackInfo ci) {
        Vector4f vec = new Vector4f((float) x, (float) y, (float) z, 1.0F);
        vec.transform(mat);
        vec.normalize();

        switch (index) {
            case 0 -> {
                this.nxX = vec.getX();
                this.nxY = vec.getY();
                this.nxZ = vec.getZ();
                this.nxW = vec.getW();
            }
            case 1 -> {
                this.pxX = vec.getX();
                this.pxY = vec.getY();
                this.pxZ = vec.getZ();
                this.pxW = vec.getW();
            }
            case 2 -> {
                this.nyX = vec.getX();
                this.nyY = vec.getY();
                this.nyZ = vec.getZ();
                this.nyW = vec.getW();
            }
            case 3 -> {
                this.pyX = vec.getX();
                this.pyY = vec.getY();
                this.pyZ = vec.getZ();
                this.pyW = vec.getW();
            }
            case 4 -> {
                this.nzX = vec.getX();
                this.nzY = vec.getY();
                this.nzZ = vec.getZ();
                this.nzW = vec.getW();
            }
            case 5 -> {
                this.pzX = vec.getX();
                this.pzY = vec.getY();
                this.pzZ = vec.getZ();
                this.pzW = vec.getW();
            }
            default -> throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override
    public boolean fastAabbTest(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        return this.isAnyCornerVisible(minX - this.xF, minY - this.yF, minZ - this.zF,
                maxX - this.xF, maxY - this.yF, maxZ - this.zF);
    }

    /**
     * @author JohanVonElectrum
     * @reason Optimize away object allocations and for-loop
     */
    @Overwrite
    private boolean isAnyCornerVisible(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        return isAnyCornerVisible(null, minX, minY, minZ, maxX, maxY, maxZ);
    }

    private boolean isAnyCornerVisible(boolean[] ref, float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        return isCornerVisible(ref, minX, minY, minZ, maxX, maxY, maxZ, this.nxX, this.nxY, this.nxZ, this.nxW) &&
                isCornerVisible(ref, minX, minY, minZ, maxX, maxY, maxZ, this.pxX, this.pxY, this.pxZ, this.pxW) &&
                isCornerVisible(ref, minX, minY, minZ, maxX, maxY, maxZ, this.nyX, this.nyY, this.nyZ, this.nyW) &&
                isCornerVisible(ref, minX, minY, minZ, maxX, maxY, maxZ, this.pyX, this.pyY, this.pyZ, this.pyW) &&
                isCornerVisible(ref, minX, minY, minZ, maxX, maxY, maxZ, this.nzX, this.nzY, this.nzZ, this.nzW) &&
                isCornerVisible(ref, minX, minY, minZ, maxX, maxY, maxZ, this.pzX, this.pzY, this.pzZ, this.pzW);
    }

    @Override
    public RenderRegionVisibility aabbTest(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        return this.aabbTest0(minX - this.xF, minY - this.yF, minZ - this.zF,
                maxX - this.xF, maxY - this.yF, maxZ - this.zF);
    }

    private RenderRegionVisibility aabbTest0(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        boolean[] inside = new boolean[] { true }; // in C++, pass boolean as reference to isCornerVisible(bool &ref, ...)

        if (isAnyCornerVisible(inside, minX, minY, minZ, maxX, maxY, maxZ))
            return inside[0] ? RenderRegionVisibility.FULLY_VISIBLE : RenderRegionVisibility.VISIBLE;

        return RenderRegionVisibility.CULLED;
    }

    private static boolean isCornerVisible(boolean[] ref, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float x, float y, float z, float w) {
        if (ref != null)
            ref[0] &= x * (x < 0 ? maxX : minX) + y * (y < 0 ? maxY : minY) + z * (z < 0 ? maxZ : minZ) >= -w;
        return x * (x < 0 ? minX : maxX) + y * (y < 0 ? minY : maxY) + z * (z < 0 ? minZ : maxZ) >= -w;
    }
}
