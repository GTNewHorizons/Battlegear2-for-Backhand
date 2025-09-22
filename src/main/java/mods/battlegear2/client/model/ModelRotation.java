package mods.battlegear2.client.model;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

/**
 * A lightweight class for storing the rotation & translation of a given model without the need of a
 * ModelRenderer/ModelBase
 */
public class ModelRotation {

    public float rotationPointX;
    public float rotationPointY;
    public float rotationPointZ;
    public float rotateAngleX;
    public float rotateAngleY;
    public float rotateAngleZ;

    public ModelRotation setRotationPoint(float p_78793_1_, float p_78793_2_, float p_78793_3_) {
        this.rotationPointX = p_78793_1_;
        this.rotationPointY = p_78793_2_;
        this.rotationPointZ = p_78793_3_;
        return this;
    }

    public ModelRotation setRotationAngles(float x, float y, float z) {
        rotateAngleX = x;
        rotateAngleY = y;
        rotateAngleZ = z;
        return this;
    }

    public ModelRotation setRotationAngleX(float x) {
        rotateAngleX = x;
        return this;
    }

    public ModelRotation setRotationAngleY(float y) {
        rotateAngleY = y;
        return this;
    }

    public ModelRotation setRotationAngleZ(float z) {
        rotateAngleZ = z;
        return this;
    }

    public Matrix4f getModelMatrix(float scale) {
        Matrix4f mat4f = new Matrix4f();
        mat4f.translate(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);

        if (this.rotateAngleZ != 0.0F) {
            mat4f.rotate(this.rotateAngleZ, 0.0F, 0.0F, 1.0F);
        }

        if (this.rotateAngleY != 0.0F) {
            mat4f.rotate(this.rotateAngleY, 0.0F, 1.0F, 0.0F);
        }

        if (this.rotateAngleX != 0.0F) {
            mat4f.rotate(this.rotateAngleX, 1.0F, 0.0F, 0.0F);
        }
        return mat4f;
    }

    public void applyTransformations(float scale) {
        GL11.glTranslatef(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);

        if (this.rotateAngleZ != 0.0F) {
            GL11.glRotatef(this.rotateAngleZ * (180F / (float) Math.PI), 0.0F, 0.0F, 1.0F);
        }

        if (this.rotateAngleY != 0.0F) {
            GL11.glRotatef(this.rotateAngleY * (180F / (float) Math.PI), 0.0F, 1.0F, 0.0F);
        }

        if (this.rotateAngleX != 0.0F) {
            GL11.glRotatef(this.rotateAngleX * (180F / (float) Math.PI), 1.0F, 0.0F, 0.0F);
        }
    }
}
