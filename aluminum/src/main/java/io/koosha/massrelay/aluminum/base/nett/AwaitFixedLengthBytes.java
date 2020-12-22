package io.koosha.massrelay.aluminum.base.nett;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public final class AwaitFixedLengthBytes extends ByteToMessageDecoder {

    private final int frameLength;

    public AwaitFixedLengthBytes(final int frameLength) {
        if (frameLength <= 0)
            throw new IllegalArgumentException("frameLength must be a positive integer: " + frameLength);

        this.frameLength = frameLength;
    }

    @Override
    public boolean isSharable() {
        return false;
    }

    @Override
    protected void decode(final ChannelHandlerContext ctx,
                          final ByteBuf in,
                          final List<Object> out) {
        if (in.readableBytes() < frameLength)
            return;

        ctx.pipeline().remove(this);
        out.add(in.readRetainedSlice(frameLength));
        if (in.readableBytes() > 0)
            out.add(in.readRetainedSlice(in.readableBytes()));
    }

}
