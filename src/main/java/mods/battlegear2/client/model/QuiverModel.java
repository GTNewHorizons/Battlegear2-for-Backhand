package mods.battlegear2.client.model;

import java.lang.Math;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

import org.joml.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.gtnewhorizon.gtnhlib.client.model.BakedModelBuilder;
import com.gtnewhorizon.gtnhlib.client.model.NormalHelper;
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

    private final VertexBuffer skeletonArrowVBO;

    private final float[][] arrowPos = new float[10][3];

    private static final float zRotation = -0.7853982F;
    private static final float zRotationDegrees = (float) Math.toDegrees(zRotation);
    private static final int ARROW_X_WIDTH = 9;

    public QuiverModel() {
        Random r = new Random(42);
        for (int i = 0; i < arrowPos.length; i++) {
            arrowPos[i] = new float[] { -r.nextFloat() * 2F / 16F * 20F, r.nextFloat() * 4F,
                    -r.nextFloat() * 3F / 16F * 20F };
        }

        quiverVBO = genQuiverVBO();

        skeletonArrowVBO = genSkeletonArrowsVBO();
    }

    private VertexBuffer genQuiverVBO() {
        final float scale = BattlegearRenderHelper.RENDER_UNIT;
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

    private VertexBuffer genSkeletonArrowsVBO() {
        Matrix4fStack matrix = new Matrix4fStack(2);
        matrix.rotate(zRotation, 0, 0, 1);
        VertexBuffer vbo = new VertexBuffer(DefaultVertexFormat.POSITION_TEXTURE_NORMAL, GL11.GL_QUADS);
        vbo.bind();
        TessellatorManager.startCapturing();
        CapturingTessellator tessellator = (CapturingTessellator) TessellatorManager.get();
        tessellator.startDrawing(GL11.GL_QUADS);
        Vector4f vec = new Vector4f();
        // Please don't ask me what any of these floats mean. I have no idea, but it works, and that's all I care about.
        matrix.translate(-14F / 16F + 0.925f, -3F / 16F + 0.475f, 2.5F / 16F);
        matrix.scale(0.05f, 0.05f, -0.05f);
        float f2 = 0.0F;
        float f3 = ARROW_X_WIDTH / 32.0F;
        float f4 = 0f;
        float f5 = 5f / 32.0F;
        float f6 = 0.0F;
        float f7 = 0.15625F;
        float f8 = 5f / 32.0F;
        float f9 = 10f / 32.0F;
        matrix.rotate((float) Math.PI, 0, 0, 1);

        Matrix3f normalMat = NormalHelper.getNormalMatrix(matrix);
        Vector3f normal = new Vector3f(-0.05f, 0, 0);
        tessellator.setNormalTransformed(normal, normalMat);
        for (int i = 0; i < SKELETON_ARROW; i++) {
            matrix.pushMatrix();
            float[] arrow = arrowPos[i];
            matrix.translate(arrow[2], arrow[1], arrow[0]);
            matrix.rotate((float) Math.PI / 4f, 1, 0, 0);

            addVertexWithUV(tessellator, vec, matrix, -7, -2, -2, f6, f8);
            addVertexWithUV(tessellator, vec, matrix, -7, -2, 2, f7, f8);
            addVertexWithUV(tessellator, vec, matrix, -7, 2, 2, f7, f9);
            addVertexWithUV(tessellator, vec, matrix, -7, 2, -2, f6, f9);

            addVertexWithUV(tessellator, vec, matrix, -7, 2, -2, f6, f8);
            addVertexWithUV(tessellator, vec, matrix, -7, 2, 2, f7, f8);
            addVertexWithUV(tessellator, vec, matrix, -7, -2, 2, f7, f9);
            addVertexWithUV(tessellator, vec, matrix, -7, -2, -2, f6, f9);

            for (int j = 0; j < 4; j++) {
                matrix.rotate((float) Math.PI / 2f, 1, 0, 0);
                addVertexWithUV(tessellator, vec, matrix, -8, -2, 0, f2, f4);
                addVertexWithUV(tessellator, vec, matrix, ARROW_X_WIDTH - 8, -2, 0, f3, f4);
                addVertexWithUV(tessellator, vec, matrix, ARROW_X_WIDTH - 8, 2, 0, f3, f5);
                addVertexWithUV(tessellator, vec, matrix, -8, 2, 0, f2, f5);
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

    // Copied from Minecraft (Battlegear's arrows rendering were weird)
    private static void renderQuiverArrows(int amount, float[][] arrowOffsets) {
        if (amount > arrowOffsets.length) amount = arrowOffsets.length;
        GL11.glRotatef(zRotationDegrees, 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef(-14F / 16F + 0.925f, -3F / 16F + 0.475f, 2.5F / 16F);
        GL11.glScalef(0.05f, 0.05f, -0.05f);

        Tessellator tessellator = Tessellator.instance;
        float f2 = 0.0F;
        float f3 = ARROW_X_WIDTH / 32F;
        float f4 = 0;
        float f5 = 5f / 32.0F;
        float f6 = 0.0F;
        float f7 = 0.15625F;
        float f8 = 5f / 32.0F;
        float f9 = 10f / 32.0F;

        float f10 = 0.05f;
        GL11.glRotatef(180, 0, 0, 1);
        GL11.glNormal3f(-f10, 0.0F, 0.0F);
        for (int i = 0; i < amount; i++) {
            GL11.glPushMatrix();
            float[] offset = arrowOffsets[i];

            GL11.glTranslatef(offset[2], offset[1], offset[0]);
            GL11.glRotatef(45.0F, 1.0F, 0.0F, 0.0F);
            tessellator.startDrawingQuads();
            tessellator.addVertexWithUV(-7.0D, -2.0D, -2.0D, f6, f8);
            tessellator.addVertexWithUV(-7.0D, -2.0D, 2.0D, f7, f8);
            tessellator.addVertexWithUV(-7.0D, 2.0D, 2.0D, f7, f9);
            tessellator.addVertexWithUV(-7.0D, 2.0D, -2.0D, f6, f9);
            tessellator.draw();

            for (int j = 0; j < 4; j++) {
                GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
                tessellator.startDrawingQuads();
                tessellator.addVertexWithUV(-8, -2, 0, f2, f4);
                tessellator.addVertexWithUV(-8 + ARROW_X_WIDTH, -2, 0, f3, f4);
                tessellator.addVertexWithUV(-8 + ARROW_X_WIDTH, 2, 0, f3, f5);
                tessellator.addVertexWithUV(-8, 2, 0, f2, f5);
                tessellator.draw();
            }

            GL11.glPopMatrix();
        }
    }

    private void addVertexWithUV(Tessellator tessellator, Vector4f vec, Matrix4f mat4f, float x, float y, float z,
            float u, float v) {
        vec.x = x;
        vec.y = y;
        vec.z = z;
        vec.w = 1;
        vec.mul(mat4f);
        tessellator.addVertexWithUV(vec.x, vec.y, vec.z, u, v);
    }

    public void renderQuiver(int arrowCount, int color) {
        GL11.glScalef(1.05F, 1.05F, 1.05F);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        final TextureManager renderEngine = Minecraft.getMinecraft().renderEngine;
        GL11.glColor3f(1, 1, 1);

        quiverVBO.setupState();
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

        renderEngine.bindTexture(BattlegearRenderHelper.DEFAULT_ARROW);
        GL11.glColor3f(0.75f, 0.75f, 0.75f);
        if (arrowCount == SKELETON_ARROW) {
            // renderPlayerArrows(arrowCount);
            skeletonArrowVBO.setupState();
            skeletonArrowVBO.draw();
            skeletonArrowVBO.cleanupState();
        } else {
            quiverVBO.cleanupState();
            renderQuiverArrows(arrowCount, arrowPos);
        }
        GL11.glColor3f(1, 1, 1);
    }
}
