package io.koosha.massrelay.aluminum.base;

import io.koosha.massrelay.aluminum.base.func.Action;
import io.koosha.massrelay.aluminum.base.func.ActionE;
import io.koosha.massrelay.aluminum.base.func.CronTask;
import io.koosha.massrelay.aluminum.base.value.Result;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Provider;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public final class Util {

    private static final Logger log = LoggerFactory.getLogger(Util.class);

    public static final AttributeKey<String> ID = AttributeKey.newInstance("client_id");

    private Util() {
    }


    public static String causeMsg(final Throwable cause) {
        try {
            if (cause == null) {
                return "?";
            }
            else if (cause instanceof NullPointerException) {
                final StringWriter sw = new StringWriter();
                final PrintWriter pw = new PrintWriter(sw);
                cause.printStackTrace(pw);
                return cause.getClass().getSimpleName()
                    + ": "
                    + cause.getMessage()
                    + " -> \n"
                    + sw.toString();
            }
            else if (cause.getMessage().isEmpty()) {
                return cause.getClass().getSimpleName();
            }
            else {
                return cause.getMessage();
            }
        }
        catch (Exception e) {
            return e.getClass().getName() + " - " + e.getMessage();
        }
    }

    public static ChannelInitializer<Channel> init(final Consumer<Channel> c) {
        return new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(final Channel ch) {
                c.accept(ch);
            }
        };
    }

    public static String socketAddrInfo(final SocketAddress sa) {
        if (sa == null)
            return "?";

        if (sa instanceof InetSocketAddress) {
            final InetSocketAddress sar = (InetSocketAddress) sa;
            final String h = sar.getHostString();
            final int p = sar.getPort();
            return h + ':' + p;
        }
        else {
            return sa.toString();
        }
    }

    public static String twoWayConnectionInfo(final Channel channel) {
        if (channel == null)
            return "?";
        return socketAddrInfo(channel.remoteAddress())
            + " / "
            + socketAddrInfo(channel.localAddress());
    }

    // -------------------------------------------------------------------------

    public static ByteBuf encode(final String... args) {
        final byte[][] encode = new byte[args.length][];
        for (int i = 0; i < args.length; i++)
            encode[i] = args[i].getBytes(StandardCharsets.UTF_8);
        return encode(encode);
    }

    public static ByteBuf encode(final byte[]... args) {
        final ByteBuf b = unpool();
        for (byte[] arg : args)
            b.writeBytes(Base64.getEncoder().encode(arg)).writeByte(0);
        return b;
    }

    public static Result<List<String>> decodeStrings(final ByteBuf unpool,
                                                     final int delimiterCount) {
        if (unpool == null || unpool.readableBytes() < 1)
            return Result.fail();

        final ByteBuf[] each = new ByteBuf[delimiterCount];
        for (int i = 0; i < delimiterCount; i++)
            each[i] = unpool();

        final AtomicInteger index = new AtomicInteger(0);
        final AtomicInteger count = new AtomicInteger(0);
        unpool.forEachByte(value -> {
            count.incrementAndGet();
            if (value == 0) {
                index.incrementAndGet();
                return index.get() != delimiterCount;
            }
            each[index.get()].writeByte(value);
            return true;
        });
        unpool.skipBytes(count.get());

        if (index.get() != delimiterCount)
            return Result.fail();

        final Base64.Decoder decoder = Base64.getDecoder();
        final List<byte[]> decode = new ArrayList<>(each.length);
        for (final ByteBuf b : each)
            decode.add(decoder.decode(readBytes(b)));

        final List<String> ret = new ArrayList<>(decode.size());
        for (final byte[] b : decode)
            ret.add(new String(b, StandardCharsets.UTF_8));

        return Result.ok(ret);
    }

    // -------------------------------------------------------------------------

    public static void doAllOfE(final String tag,
                                final ActionE... actionES) {
        for (final ActionE action : actionES)
            try {
                action.exec();
            }
            catch (Exception e) {
                log.warn("{} -> doAll action encountered an exception: ", tag, e);
            }
    }

    public static void doAllOf(final String tag,
                               final Action... actions) {
        for (final Action action : actions)
            try {
                action.exec();
            }
            catch (Exception e) {
                log.warn("{} -> doAll action encountered an exception: ", tag, e);
            }
    }

    public static Action delayedAction(final String tag,
                                       final long delay,
                                       final Action action) {
        return () -> new Thread(() -> {
            Thread.currentThread().setName(tag);
            if (delay > 0)
                try {
                    Thread.sleep(delay);
                }
                catch (InterruptedException e) {
                    log.warn("{} -> could not sleep before task, executing immediately", tag);
                }
            try {
                action.exec();
            }
            catch (final Throwable throwable) {
                log.warn("{} -> delayed task failed", tag);
                throw throwable;
            }
        }).start();
    }

    public static void cron(final ScheduledExecutorService ses,
                            final long delay,
                            final Provider<? extends CronTask> task) {
        ses.scheduleWithFixedDelay(() -> {
            try {
                task.get().exec();
            }
            catch (final Exception e) {
                log.error("cron task failed", e);
            }
        }, delay, delay, TimeUnit.MILLISECONDS);
    }

    public static void cron(final ScheduledExecutorService ses,
                            final long delay,
                            final ActionE task) {
        cron(ses, delay, () -> CronTask.of(task));
    }

    public static void bg(final String tag,
                          final Action action) {
        bg(tag, action, it -> {
        }, () -> {
        });
    }

    public static void bgOr(final String tag,
                            final Action action,
                            final Consumer<Exception> elseOnFail) {
        bg(tag, action, elseOnFail, () -> {
        });
    }

    public static void bg(final String tag,
                          final Action action,
                          final Consumer<Exception> elseOnFail,
                          final Action onEnd) {
        new Thread(() -> {
            Thread.currentThread().setName(tag);
            try {
                action.exec();
            }
            catch (final Exception e) {
                log.warn("{} -> bg failed", tag);
                elseOnFail.accept(e);
            }
            finally {
                onEnd.exec();
            }
        }, tag).start();
    }

    // ------------------------------------------------------------------------

    public static boolean ensureAtLeastBytes(final ChannelHandlerContext ctx,
                                             final ByteBuf b,
                                             final int len) {
        if (len < 1)
            throw new IndexOutOfBoundsException("len: " + len);

        if (b.readableBytes() >= len)
            return true;

        final String info = twoWayConnectionInfo(ctx.channel());
        final String id = ctx.channel().attr(ID).get();

        final String msg = "bad number of bytes: {}, expected >= {}, on: {} - {}";

        log.warn(msg, b.readableBytes(), len, id, info);

        ctx.close();
        return false;
    }

    private static boolean ensureExactBytes(final ChannelHandlerContext ctx,
                                            final ByteBuf b,
                                            @SuppressWarnings("SameParameterValue") final int len) {
        if (len < 1)
            throw new IndexOutOfBoundsException("len: " + len);

        if (b.readableBytes() == len)
            return true;

        final String info = twoWayConnectionInfo(ctx.channel());
        final String id = ctx.channel().attr(ID).get();
        final String msg = "bad number of bytes: {}, expected == {}, on: {} - {}";

        log.warn(msg, b.readableBytes(), len, id, info);

        ctx.close();
        return false;
    }

    private static boolean ensureAtMostBytes(final ChannelHandlerContext ctx,
                                             final ByteBuf b,
                                             final int len) {
        if (len < 1)
            throw new IndexOutOfBoundsException("len: " + len);

        if (b.readableBytes() <= len)
            return true;

        final String info = twoWayConnectionInfo(ctx.channel());
        final String id = ctx.channel().attr(ID).get();
        final String msg = "bad number of bytes: {}, expected <= {}, on: {} - {}";

        log.warn(msg, b.readableBytes(), len, id, info);

        ctx.close();
        return false;
    }

    public static boolean ensureBytesRange(final ChannelHandlerContext ctx,
                                           final ByteBuf b,
                                           final int min,
                                           final int max) {
        if (max < min)
            throw new IllegalArgumentException("bad range: " + min + "~" + max);
        return ensureAtLeastBytes(ctx, b, min) && ensureAtMostBytes(ctx, b, max);
    }

    public static boolean ensureAtLeastByte(final ChannelHandlerContext ctx,
                                            final ByteBuf b) {
        return ensureAtLeastBytes(ctx, b, 1);
    }

    public static boolean ensureExactOneByte(final ChannelHandlerContext ctx,
                                             final ByteBuf b) {
        return ensureExactBytes(ctx, b, 1);
    }

    public static byte[] readBytes(final ByteBuf msg) {
        final byte[] r = new byte[msg.readableBytes()];
        msg.readBytes(r);
        return r;
    }

    public static byte[] getBytes(final ByteBuf msg) {
        final byte[] r = new byte[msg.readableBytes()];
        msg.getBytes(msg.readerIndex(), r);
        return r;
    }

    public static String read(final ByteBuf msg) {
        if (msg.readableBytes() == 0)
            return "";
        final byte[] read = new byte[msg.readableBytes()];
        msg.readBytes(read);
        return new String(read, StandardCharsets.UTF_8);
    }

    public static ByteBuf unpool() {
        return Unpooled.buffer();
    }

    public static ByteBuf unpool(final int i) {
        return unpool().writeInt(i);
    }

    public static ByteBuf unpool(final byte b) {
        return unpool().writeByte(b);
    }

    public static ByteBuf unpool(final byte[] bytes) {
        return unpool().writeBytes(bytes);
    }

    public static ByteBuf unpool(final ByteBuf bytes) {
        return unpool().writeBytes(bytes.copy());
    }

}
