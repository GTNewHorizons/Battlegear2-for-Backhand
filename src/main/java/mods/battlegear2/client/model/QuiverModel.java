package mods.battlegear2.client.model;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import com.gtnewhorizon.gtnhlib.client.renderer.CapturingTessellator;
import com.gtnewhorizon.gtnhlib.client.renderer.TessellatorManager;
import com.gtnewhorizon.gtnhlib.client.renderer.quad.QuadView;
import com.gtnewhorizon.gtnhlib.client.renderer.vbo.VertexBuffer;
import com.gtnewhorizon.gtnhlib.client.renderer.vertex.DefaultVertexFormat;

import mods.battlegear2.client.utils.BattlegearRenderHelper;

public class QuiverModel {

    public static final int SKELETON_ARROW = 5;

    private static final ResourceLocation quiverDetails = new ResourceLocation(
            "battlegear2",
            "textures/armours/quiver/QuiverDetails.png");
    private static final ResourceLocation quiverBaseTexture = new ResourceLocation(
            "battlegear2",
            "textures/armours/quiver/QuiverBase.png");

    private final VertexBuffer quiverVBO;
    private final ModelRotation quiverModel;

    private final VertexBuffer skeletonArrowVBO;

    private final float[][] arrowPos = new float[10][3];

    public QuiverModel() {
        Random r = new Random(42);
        for (int i = 0; i < arrowPos.length; i++) {
            arrowPos[i] = new float[] { -r.nextFloat() * 2F / 16F * 20F, -r.nextFloat() * 4F,
                    r.nextFloat() * 3F / 16F * 20F };
        }

        final float scale = BattlegearRenderHelper.RENDER_UNIT;

        quiverModel = new ModelRotation().setRotationPoint(0, 0, 0).setRotationAngleZ(-0.7853982F);

        quiverVBO = genQuiverVBO(scale);

        // quiverBase = new ModelRenderer(this, 0, 0);
        // quiverBase.setRotationPoint(0.0F, 0.0F, 0.0F);
        // quiverBase.setTextureSize(64, 32);
        // quiverBase.showModel = true;
        // setRotation(quiverBase, -0.7853982F);

        skeletonArrowVBO = genSkeletonArrowsVBO(scale);
    }

    private VertexBuffer genQuiverVBO(float scale) {
        BakedModelBuilder model = new BakedModelBuilder(64, 32);

        // spotless:off
        model.setTextureOffset(16, 16)
                .setRotationPoint(0.0F, 0.0F, 0.0F)
                .addBoxVertices(-4F, 0.0F, -2F, 8, 12, 4, scale);

        model.setTextureOffset(0, 0)
                .setRotationPoint(0.0F, 0.0F, 0.0F)
                .setRotationAngleZ(-0.7853982F)
                .addBoxVertices(-6F, 1.0F, 2.0F, 8, 4, 3, scale);

        model.setTextureOffset(0, 16)
                .setRotationPoint(-3F, 5F, 2.0F)
                .setRotationAngleZ(0.2617994F)
                .addBoxVertices(-0.6F, 0.1F, 0.0F, 1, 2, 3, scale);

        model.setTextureOffset(0, 11)
                .setRotationPoint(-2.7F, 5.5F, 2.0F)
                .setRotationAngleZ(0.7853982F)
                .addBoxVertices(0.0F, 0.0F, 0.0F, 2, 2, 3, scale);

        model.setTextureOffset(0, 21)
                .setRotationPoint(-2.7F, 8.3F, 2.0F)
                .setRotationAngleZ(-1.832596F)
                .addBoxVertices(0.0F, 0.0F, 0.0F, 1, 2, 3, scale);

        model.setTextureOffset(0, 7)
                .setRotationPoint(0.0F, 0.0F, 2.0F)
                .setRotationAngleZ(0.7853982F)
                .addBoxVertices(1.0F, -3F, 0.0F, 4, 1, 3, scale);
        //spotless:on

        return model.finish(DefaultVertexFormat.POSITION_TEXTURE_NORMAL);
    }

    private VertexBuffer genSkeletonArrowsVBO(float scale) {
        Matrix4fStack matrix = new Matrix4fStack(2);
        matrix.set(quiverModel.getModelMatrix(scale));
        VertexBuffer vbo = new VertexBuffer(DefaultVertexFormat.POSITION_TEXTURE_NORMAL, GL11.GL_QUADS);
        vbo.bind();
        TessellatorManager.startCapturing();
        CapturingTessellator tessellator = (CapturingTessellator) TessellatorManager.get();
        tessellator.startDrawing(GL11.GL_QUADS);
        Vector4f vec = new Vector4f();
        // Please don't ask me what any of these variables mean. I have no idea, but it works, and that's all that
        // matters.
        final float u = 12F / 32F;
        final float v = 5F / 32.0F;
        matrix.translate(-14F / 16F + 0.525f, -3F / 16F + 0.475f, 2.5F / 16F);
        matrix.scale(0.05f, 0.05f, -0.05f);
        for (int i = 0; i < SKELETON_ARROW; i++) {
            matrix.pushMatrix();
            float[] arrow = arrowPos[i];
            matrix.translate(arrow[2], arrow[1], arrow[0]);

            Matrix4f mat4f = new Matrix4f();
            for (int j = 0; j < 2; j++) {
                matrix.rotate((float) Math.toRadians(90f), 1, 0, 0);
                mat4f.rotate((float) Math.toRadians(-90f), 1, 0, 0);
                Vector4f transformed = mat4f.transform(new Vector4f(0, 0, 1, 0));
                tessellator.setNormal(transformed.x, transformed.y, transformed.z);
                addVector(tessellator, vec, matrix, 0, -2, 0, u, 0);
                addVector(tessellator, vec, matrix, 16, -2, 0, 0, 0);
                addVector(tessellator, vec, matrix, 16, 2, 0, 0, v);
                addVector(tessellator, vec, matrix, 0, 2, 0, u, v);

                addVector(tessellator, vec, matrix, 0, 2, 0, u, v);
                addVector(tessellator, vec, matrix, 16, 2, 0, 0, v);
                addVector(tessellator, vec, matrix, 16, -2, 0, 0, 0);
                addVector(tessellator, vec, matrix, 0, -2, 0, u, 0);
            }
            matrix.popMatrix();
        }
        List<QuadView> quads = TessellatorManager.stopCapturingToPooledQuads();
        ByteBuffer bytes = CapturingTessellator.quadsToBuffer(quads, DefaultVertexFormat.POSITION_TEXTURE_NORMAL);
        vbo.upload(bytes);
        vbo.unbind();

        tessellator.clearQuads();
        return vbo;
    }

    private void addVector(Tessellator tessellator, Vector4f vec, Matrix4f mat4f, int x, int y, int z, float u,
            float v) {
        vec.x = x;
        vec.y = y;
        vec.z = z;
        vec.w = 1;
        vec.mul(mat4f);
        tessellator.addVertexWithUV(vec.x, vec.y, vec.z, u, v);
    }

    public void render() {
        quiverVBO.render();
    }

    public void renderPlayerArrows(int arrowCount, float scale) {
        if (arrowCount > arrowPos.length) arrowCount = arrowPos.length;
        GL11.glPushMatrix();

        quiverModel.applyTransformations(scale);
        BattlegearRenderHelper
                .batchRenderArrows(-14F / 16F + 0.525f, -3F / 16F + 0.475f, 2.5F / 16F, arrowCount, arrowPos);

        GL11.glPopMatrix();
    }

    public void renderQuiver(int arrowCount, float scale, int color) {
        TextureManager renderEngine = Minecraft.getMinecraft().renderEngine;
        renderEngine.bindTexture(BattlegearRenderHelper.DEFAULT_ARROW);
        if (arrowCount == SKELETON_ARROW) {
            skeletonArrowVBO.setupState();
            skeletonArrowVBO.close();
            skeletonArrowVBO.draw();
            // No need to unbind, will be done at the end of the loop.
        } else {
            renderPlayerArrows(arrowCount, scale);
        }
        quiverVBO.setupState();
        // quiverVBO.setupState();
        renderEngine.bindTexture(quiverDetails);
        quiverVBO.draw();

        renderEngine.bindTexture(quiverBaseTexture);
        if (color == -1) {
            GL11.glColor3f(0.10F, 0.10F, 0.10F);
        } else {
            float red = (float) (color >> 16 & 255) / 255.0F;
            float green = (float) (color >> 8 & 255) / 255.0F;
            float blue = (float) (color & 255) / 255.0F;
            GL11.glColor3f(red, green, blue);
        }
        quiverVBO.draw();
        GL11.glColor3f(1, 1, 1);

        quiverVBO.cleanupState();
    }

    private void setRotation(ModelRenderer model, float z) {
        model.rotateAngleZ = z;
    }
}
