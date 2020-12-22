package io.koosha.massrelay.aluminum.base.nett;

import io.koosha.massrelay.aluminum.base.TaskGuard;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

public final class TaskGuardHandler extends ChannelDuplexHandler {

    private final TaskGuard taskGuard;

    public TaskGuardHandler(final TaskGuard taskGuard) {
        this.taskGuard = taskGuard;
    }

    @Override
    public void read(final ChannelHandlerContext ctx) throws Exception {
        this.taskGuard.reset();
        super.read(ctx);
    }

    @Override
    public void write(final ChannelHandlerContext ctx,
                      final Object msg,
                      final ChannelPromise promise) throws Exception {
        this.taskGuard.reset();
        super.write(ctx, msg, promise);
    }

    @Override
    public void close(final ChannelHandlerContext ctx,
                      final ChannelPromise promise) throws Exception {
        this.taskGuard.stop();
        super.close(ctx, promise);
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
        this.taskGuard.stop();
        super.channelInactive(ctx);
    }

}
