package android.filterpacks.imageproc;

import android.app.slice.SliceItem;
import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext;
import android.filterfw.core.Frame;
import android.filterfw.core.FrameFormat;
import android.filterfw.core.GenerateFieldPort;
import android.filterfw.core.Program;
import android.filterfw.core.ShaderProgram;
import android.filterfw.format.ImageFormat;
import android.filterfw.geometry.Point;
import android.filterfw.geometry.Quad;

public class RotateFilter extends Filter {
    @GenerateFieldPort(name = "angle")
    private int mAngle;
    private int mHeight = 0;
    private int mOutputHeight;
    private int mOutputWidth;
    private Program mProgram;
    private int mTarget = 0;
    @GenerateFieldPort(hasDefault = true, name = "tile_size")
    private int mTileSize = 640;
    private int mWidth = 0;

    public RotateFilter(String name) {
        super(name);
    }

    public void setupPorts() {
        addMaskedInputPort(SliceItem.FORMAT_IMAGE, ImageFormat.create(3));
        addOutputBasedOnInput(SliceItem.FORMAT_IMAGE, SliceItem.FORMAT_IMAGE);
    }

    public void initProgram(FilterContext context, int target) {
        if (target == 3) {
            ShaderProgram shaderProgram = ShaderProgram.createIdentity(context);
            shaderProgram.setMaximumTileSize(this.mTileSize);
            shaderProgram.setClearsOutput(true);
            this.mProgram = shaderProgram;
            this.mTarget = target;
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Filter Sharpen does not support frames of target ");
        stringBuilder.append(target);
        stringBuilder.append("!");
        throw new RuntimeException(stringBuilder.toString());
    }

    public void fieldPortValueUpdated(String name, FilterContext context) {
        if (this.mProgram != null) {
            updateParameters();
        }
    }

    public void process(FilterContext context) {
        Frame input = pullInput(SliceItem.FORMAT_IMAGE);
        FrameFormat inputFormat = input.getFormat();
        if (this.mProgram == null || inputFormat.getTarget() != this.mTarget) {
            initProgram(context, inputFormat.getTarget());
        }
        if (!(inputFormat.getWidth() == this.mWidth && inputFormat.getHeight() == this.mHeight)) {
            this.mWidth = inputFormat.getWidth();
            this.mHeight = inputFormat.getHeight();
            this.mOutputWidth = this.mWidth;
            this.mOutputHeight = this.mHeight;
            updateParameters();
        }
        Frame output = context.getFrameManager().newFrame(ImageFormat.create(this.mOutputWidth, this.mOutputHeight, 3, 3));
        this.mProgram.process(input, output);
        pushOutput(SliceItem.FORMAT_IMAGE, output);
        output.release();
    }

    private void updateParameters() {
        if (this.mAngle % 90 == 0) {
            float sinTheta;
            float sinTheta2 = -1.0f;
            if (this.mAngle % 180 == 0) {
                sinTheta = 0.0f;
                if (this.mAngle % 360 == 0) {
                    sinTheta2 = 1.0f;
                }
            } else {
                if ((this.mAngle + 90) % 360 != 0) {
                    sinTheta2 = 1.0f;
                }
                this.mOutputWidth = this.mHeight;
                this.mOutputHeight = this.mWidth;
                float f = sinTheta2;
                sinTheta2 = 0.0f;
                sinTheta = f;
            }
            ((ShaderProgram) this.mProgram).setTargetRegion(new Quad(new Point((((-sinTheta2) + sinTheta) + 1.0f) * 0.5f, (((-sinTheta) - sinTheta2) + 1.0f) * 0.5f), new Point(((sinTheta2 + sinTheta) + 1.0f) * 0.5f, ((sinTheta - sinTheta2) + 1.0f) * 0.5f), new Point((((-sinTheta2) - sinTheta) + 1.0f) * 0.5f, (((-sinTheta) + sinTheta2) + 1.0f) * 0.5f), new Point(((sinTheta2 - sinTheta) + 1.0f) * 0.5f, 0.5f * ((sinTheta + sinTheta2) + 1.0f))));
            return;
        }
        throw new RuntimeException("degree has to be multiply of 90.");
    }
}
