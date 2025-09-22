package mods.battlegear2.client.model;

import java.nio.ByteBuffer;
import java.util.List;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.PositionTextureVertex;
import net.minecraft.client.model.TexturedQuad;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.Vec3;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import com.gtnewhorizon.gtnhlib.client.renderer.CapturingTessellator;
import com.gtnewhorizon.gtnhlib.client.renderer.TessellatorManager;
import com.gtnewhorizon.gtnhlib.client.renderer.quad.QuadView;
import com.gtnewhorizon.gtnhlib.client.renderer.vbo.VertexBuffer;
import com.gtnewhorizon.gtnhlib.client.renderer.vertex.VertexFormat;

public class BakedModelBuilder {

    public float textureWidth;
    public float textureHeight;
    private int textureOffsetX;
    private int textureOffsetY;
    public float rotationPointX;
    public float rotationPointY;
    public float rotationPointZ;
    // Note: These are in radians, not degrees. Same as ModelRenderer
    public float rotateAngleX;
    public float rotateAngleY;
    public float rotateAngleZ;
    public boolean mirror;
    public final CapturingTessellator tessellator;

    public BakedModelBuilder(int textureWidth, int textureHeight) {
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;

        TessellatorManager.startCapturing();
        this.tessellator = (CapturingTessellator) TessellatorManager.get();
        tessellator.startDrawing(GL11.GL_QUADS);
    }

    public BakedModelBuilder(ModelBase model) {
        this(model.textureWidth, model.textureHeight);
    }

    public BakedModelBuilder(ModelBase p_i1174_1_, int p_i1174_2_, int p_i1174_3_) {
        this(p_i1174_1_);
        this.setTextureOffset(p_i1174_2_, p_i1174_3_);
    }

    public BakedModelBuilder setTextureOffset(int p_78784_1_, int p_78784_2_) {
        this.textureOffsetX = p_78784_1_;
        this.textureOffsetY = p_78784_2_;
        return this;
    }

    public BakedModelBuilder setTextureSize(int textureWidth, int textureHeight) {
        this.textureWidth = (float) textureWidth;
        this.textureHeight = (float) textureHeight;
        return this;
    }

    public BakedModelBuilder setRotationPoint(float p_78793_1_, float p_78793_2_, float p_78793_3_) {
        this.rotationPointX = p_78793_1_;
        this.rotationPointY = p_78793_2_;
        this.rotationPointZ = p_78793_3_;
        return this;
    }

    public BakedModelBuilder setRotationAngles(float x, float y, float z) {
        rotateAngleX = x;
        rotateAngleY = y;
        rotateAngleZ = z;
        return this;
    }

    public BakedModelBuilder setRotationAngleX(float x) {
        rotateAngleX = x;
        return this;
    }

    public BakedModelBuilder setRotationAngleY(float y) {
        rotateAngleY = y;
        return this;
    }

    public BakedModelBuilder setRotationAngleZ(float z) {
        rotateAngleZ = z;
        return this;
    }

    public BakedModelBuilder addBoxVertices(float x1, float y1, float z1, int xWidth, int yWidth, int zWidth,
            float scale) {
        return addBoxVertices(x1, y1, z1, xWidth, yWidth, zWidth, getModelMatrix(scale), scale);
    }

    public BakedModelBuilder addBoxVertices(float x1, float y1, float z1, int xWidth, int yWidth, int zWidth,
            Matrix4f mat4f, float scale) {
        new ModelBox(this, this.textureOffsetX, this.textureOffsetY, x1, y1, z1, xWidth, yWidth, zWidth, 0.0F)
                .addVerticesToTesselator(tessellator, mat4f, scale);
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

    public VertexBuffer finish(VertexFormat format) {
        VertexBuffer vertexBuffer = new VertexBuffer(format, GL11.GL_QUADS);
        vertexBuffer.bind();
        List<QuadView> quads = TessellatorManager.stopCapturingToPooledQuads();
        ByteBuffer bytes = CapturingTessellator.quadsToBuffer(quads, format);
        vertexBuffer.upload(bytes);
        tessellator.clearQuads();

        vertexBuffer.unbind();

        return vertexBuffer;
    }

    // spotless:off
    // Mostly copied from net.minecraft.client.model.ModelBox
    private static class ModelBox {
        private final TexturedQuad[] quadList;

        public ModelBox(BakedModelBuilder p_i1171_1_, int p_i1171_2_, int p_i1171_3_, float p_i1171_4_, float p_i1171_5_, float p_i1171_6_, int p_i1171_7_, int p_i1171_8_, int p_i1171_9_, float p_i1171_10_) {
            this.quadList = new TexturedQuad[6];
            float f4 = p_i1171_4_ + (float) p_i1171_7_;
            float f5 = p_i1171_5_ + (float) p_i1171_8_;
            float f6 = p_i1171_6_ + (float) p_i1171_9_;
            p_i1171_4_ -= p_i1171_10_;
            p_i1171_5_ -= p_i1171_10_;
            p_i1171_6_ -= p_i1171_10_;
            f4 += p_i1171_10_;
            f5 += p_i1171_10_;
            f6 += p_i1171_10_;

            if (p_i1171_1_.mirror) {
                float f7 = f4;
                f4 = p_i1171_4_;
                p_i1171_4_ = f7;
            }

            PositionTextureVertex positiontexturevertex7 = new PositionTextureVertex(p_i1171_4_, p_i1171_5_, p_i1171_6_, 0.0F, 0.0F);
            PositionTextureVertex positiontexturevertex = new PositionTextureVertex(f4, p_i1171_5_, p_i1171_6_, 0.0F, 8.0F);
            PositionTextureVertex positiontexturevertex1 = new PositionTextureVertex(f4, f5, p_i1171_6_, 8.0F, 8.0F);
            PositionTextureVertex positiontexturevertex2 = new PositionTextureVertex(p_i1171_4_, f5, p_i1171_6_, 8.0F, 0.0F);
            PositionTextureVertex positiontexturevertex3 = new PositionTextureVertex(p_i1171_4_, p_i1171_5_, f6, 0.0F, 0.0F);
            PositionTextureVertex positiontexturevertex4 = new PositionTextureVertex(f4, p_i1171_5_, f6, 0.0F, 8.0F);
            PositionTextureVertex positiontexturevertex5 = new PositionTextureVertex(f4, f5, f6, 8.0F, 8.0F);
            PositionTextureVertex positiontexturevertex6 = new PositionTextureVertex(p_i1171_4_, f5, f6, 8.0F, 0.0F);
            this.quadList[0] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex4, positiontexturevertex, positiontexturevertex1, positiontexturevertex5}, p_i1171_2_ + p_i1171_9_ + p_i1171_7_, p_i1171_3_ + p_i1171_9_, p_i1171_2_ + p_i1171_9_ + p_i1171_7_ + p_i1171_9_, p_i1171_3_ + p_i1171_9_ + p_i1171_8_, p_i1171_1_.textureWidth, p_i1171_1_.textureHeight);
            this.quadList[1] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex7, positiontexturevertex3, positiontexturevertex6, positiontexturevertex2}, p_i1171_2_, p_i1171_3_ + p_i1171_9_, p_i1171_2_ + p_i1171_9_, p_i1171_3_ + p_i1171_9_ + p_i1171_8_, p_i1171_1_.textureWidth, p_i1171_1_.textureHeight);
            this.quadList[2] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex4, positiontexturevertex3, positiontexturevertex7, positiontexturevertex}, p_i1171_2_ + p_i1171_9_, p_i1171_3_, p_i1171_2_ + p_i1171_9_ + p_i1171_7_, p_i1171_3_ + p_i1171_9_, p_i1171_1_.textureWidth, p_i1171_1_.textureHeight);
            this.quadList[3] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex1, positiontexturevertex2, positiontexturevertex6, positiontexturevertex5}, p_i1171_2_ + p_i1171_9_ + p_i1171_7_, p_i1171_3_ + p_i1171_9_, p_i1171_2_ + p_i1171_9_ + p_i1171_7_ + p_i1171_7_, p_i1171_3_, p_i1171_1_.textureWidth, p_i1171_1_.textureHeight);
            this.quadList[4] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex, positiontexturevertex7, positiontexturevertex2, positiontexturevertex1}, p_i1171_2_ + p_i1171_9_, p_i1171_3_ + p_i1171_9_, p_i1171_2_ + p_i1171_9_ + p_i1171_7_, p_i1171_3_ + p_i1171_9_ + p_i1171_8_, p_i1171_1_.textureWidth, p_i1171_1_.textureHeight);
            this.quadList[5] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex3, positiontexturevertex4, positiontexturevertex5, positiontexturevertex6}, p_i1171_2_ + p_i1171_9_ + p_i1171_7_ + p_i1171_9_, p_i1171_3_ + p_i1171_9_, p_i1171_2_ + p_i1171_9_ + p_i1171_7_ + p_i1171_9_ + p_i1171_7_, p_i1171_3_ + p_i1171_9_ + p_i1171_8_, p_i1171_1_.textureWidth, p_i1171_1_.textureHeight);

            if (p_i1171_1_.mirror) {
                for (TexturedQuad texturedQuad : this.quadList) {
                    texturedQuad.flipFace();
                }
            }
        }

        public void addVerticesToTesselator(Tessellator tessellator, Matrix4f mat4f, float scale) {
            Vector4f vector = new Vector4f();
            Matrix4f normalMat = new Matrix4f(mat4f).invert().transpose();
            for (TexturedQuad quad : this.quadList) { //TODO
                PositionTextureVertex[] vertices = quad.vertexPositions;
                Vector4f normal = getNormal(vertices[0].vector3D, vertices[1].vector3D, vertices[2].vector3D, normalMat);
                tessellator.setNormal(normal.x, normal.y, normal.z);
                // Reverse so it works with culling
                for (int i = 3; i >= 0; i--) {
                    PositionTextureVertex vertex = quad.vertexPositions[i];
                    addVertexWithUV(
                            tessellator, vector, mat4f,
                            (float) (vertex.vector3D.xCoord * scale),
                            (float) (vertex.vector3D.yCoord * scale),
                            (float) (vertex.vector3D.zCoord * scale),
                            vertex.texturePositionX, vertex.texturePositionY
                    );
                }
            }
        }

        private Vector4f getNormal(Vec3 v0, Vec3 v1, Vec3 v2, Matrix4f normalsMat) {
            Vec3 vec3 = v1.subtract(v0);
            Vec3 vec31 = v1.subtract(v2);
            Vec3 vec32 = vec31.crossProduct(vec3);
            Vector4f normal = new Vector4f((float) vec32.xCoord, (float) vec32.yCoord, (float) vec32.zCoord, 0);
            return normalsMat.transform(normal).normalize();
        }

        private void addVertexWithUV(Tessellator tessellator, Vector4f vec, Matrix4f mat4f, float x, float y, float z, float u,
                               float v) {
            vec.x = x;
            vec.y = y;
            vec.z = z;
            vec.w = 1;
            vec.mul(mat4f);
            tessellator.addVertexWithUV(vec.x, vec.y, vec.z, u, v);
        }
    }
    //spotless:on
}
