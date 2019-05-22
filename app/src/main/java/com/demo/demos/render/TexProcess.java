package com.demo.demos.render;

import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;

import com.demo.demos.R;
import com.demo.demos.utils.GLUtil;

import java.nio.FloatBuffer;

import static android.opengl.GLES30.*;
import static com.demo.demos.utils.GLUtil.UNIFORM_TEXTURE;
import static com.demo.demos.utils.GLUtil.VERTEX_ATTRIB_POSITION;
import static com.demo.demos.utils.GLUtil.VERTEX_ATTRIB_POSITION_SIZE;
import static com.demo.demos.utils.GLUtil.VERTEX_ATTRIB_TEXTURE_POSITION;
import static com.demo.demos.utils.GLUtil.VERTEX_ATTRIB_TEXTURE_POSITION_SIZE;
import static com.demo.demos.utils.GLUtil.textureCoordOes;
import static com.demo.demos.utils.GLUtil.vertex;

/**
 * Created by wangyt on 2019/5/22
 */
public class TexProcess {
    private SurfaceTexture surfaceTexture;

    private FloatBuffer vertexBuffer;
    private FloatBuffer textureCoordBuffer;

    private int[] frameBuffer = new int[1];
    private int[] frameTexture = new int[1];
    private int[] cameraTexture = new int[1];

    private int program;
    private int hVertex, hTextureCoord, hTexture;

    private int width, height;

    public SurfaceTexture getSurfaceTexture(){
        return surfaceTexture;
    }

    public int[] getOutputTexture(){
        return frameTexture;
    }

    public TexProcess() {
        initBuffer();
    }

    public void onSurfaceCreated(){
        createTexture();
        surfaceTexture = new SurfaceTexture(cameraTexture[0]);

        createProgram();
        getAttribLocations();
    }

    public void onSurfaceChanged(int width, int height){
        if(this.width!=width||this.height!=height){
            this.width = width;
            this.height = height;
            delFrameBufferAndTexture();
            genFrameBufferAndTexture();
        }
    }

    public void onDraw(){
        if (surfaceTexture != null){
            surfaceTexture.updateTexImage();
        }
        bindFrameBufferAndTexture();
        draw();
        unBindFrameBuffer();
    }

    private void initBuffer(){
        vertexBuffer = GLUtil.getFloatBuffer(vertex);
        textureCoordBuffer = GLUtil.getFloatBuffer(textureCoordOes);
    }

    private void createTexture(){
        glGenTextures(cameraTexture.length, cameraTexture, 0);
    }

    private void createProgram(){
        program = GLUtil.createAndLinkProgram(R.raw.texture_vertex_shader, R.raw.texture_oes_fragtment_shader);
    }

    private void getAttribLocations(){
        hVertex = glGetAttribLocation(program, VERTEX_ATTRIB_POSITION);
        hTextureCoord = glGetAttribLocation(program, VERTEX_ATTRIB_TEXTURE_POSITION);
        hTexture = glGetUniformLocation(program, UNIFORM_TEXTURE);
    }

    private void genFrameBufferAndTexture(){
        glGenFramebuffers(frameBuffer.length, frameBuffer, 0);

        glGenTextures(frameTexture.length, frameTexture, 0);
        glBindTexture(GL_TEXTURE_2D, frameTexture[0]);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0,GL_RGBA,GL_UNSIGNED_BYTE,null);
        setTextureParameters();
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    private void setTextureParameters(){
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER,GL_NEAREST);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER,GL_LINEAR);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S,GL_CLAMP_TO_EDGE);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T,GL_CLAMP_TO_EDGE);
    }

    private void bindFrameBufferAndTexture(){
        glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer[0]);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,GL_TEXTURE_2D, frameTexture[0],0);
    }

    private void unBindFrameBuffer(){
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    private void delFrameBufferAndTexture(){
        glDeleteFramebuffers(frameBuffer.length, frameBuffer, 0);
        glDeleteTextures(frameTexture.length, frameTexture, 0);
    }

    private void draw(){
        glViewport(0,0,width,height);

        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glUseProgram(program);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, cameraTexture[0]);
        glUniform1i(hTexture, 0);

        glEnableVertexAttribArray(hVertex);
        glEnableVertexAttribArray(hTextureCoord);
        glVertexAttribPointer(hVertex,
                VERTEX_ATTRIB_POSITION_SIZE,
                GL_FLOAT,
                false,
                0,
                vertexBuffer);

        glVertexAttribPointer(hTextureCoord,
                VERTEX_ATTRIB_TEXTURE_POSITION_SIZE,
                GL_FLOAT,
                false,
                0,
                textureCoordBuffer);
        glDrawArrays(GL_TRIANGLE_FAN,0,vertex.length / 3);
        glDisableVertexAttribArray(hVertex);
        glDisableVertexAttribArray(hTextureCoord);
    }
}
