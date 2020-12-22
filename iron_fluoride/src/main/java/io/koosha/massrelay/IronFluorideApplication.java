package io.koosha.massrelay;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.fazecast.jSerialComm.SerialPort;
import io.koosha.massrelay.aluminum.base.fazecast.ComServiceFazecast;
import io.koosha.massrelay.iron.ZIronFluorideAppRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class IronFluorideApplication {

    private static final Logger log = LoggerFactory.getLogger(IronFluorideApplication.class);

    private static final String APP_NAME = "Iron Fluoride";

    public static void main(final String... args) throws Exception {
        if (!parse(args))
            return;

        SpringApplication.run(IronFluorideApplication.class, args)
                         .getBean(ZIronFluorideAppRunner.class)
                         .run();
    }

    // ==========================================

    static boolean parse(final String... args) throws Exception {
        if (args.length == 0)
            return true;

        final Args z;
        try {
            z = Args.parse(args);
        }
        catch (final ParameterException p) {
            log.error(p.getMessage());
            System.exit(1);
            return false;
        }

        if (z.help) {
            System.exit(0);
            return false;
        }

        if (z.list) {
            log.info("com port list");
            for (final SerialPort port : SerialPort.getCommPorts())
                log.info("\n{}name\t=\t{}\nfull\t=\t{}\ndamn\t=\t{}\n\n",
                    port.getSystemPortName(),
                    port.getSystemPortName(),
                    port.getDescriptivePortName(),
                    ComServiceFazecast.getDamnName(port));
            System.exit(0);
            return false;
        }

        if (z.checkAccess != null) {
            log.info("checking port accessibility...");
            final ComServiceFazecast com = new ComServiceFazecast(z.checkAccess, false);
            final boolean ok = com.makeWritable();
            com.clearPort();
            if (!ok)
                log.info("not accessible");
            else
                log.info("accessible");
            System.exit(ok ? 0 : 1);
            return false;
        }

        if (z.serialPort != null)
            ComServiceFazecast.setStaticCom(z.serialPort);

        return true;
    }

    public static final class Args {
        @Parameter(names = {"-c", "--check-access"},
                   description = "check if port is accessible and may be used",
                   arity = 1,
                   converter = SerialPortConverter.class,
                   validateWith = SerialPortValidator.class)
        SerialPort checkAccess;

        @Parameter(names = {"-s", "--serial-port"},
                   description = "serial port to use and relay to remote",
                   arity = 1,
                   converter = SerialPortConverter.class,
                   validateWith = SerialPortValidator.class)
        SerialPort serialPort;

        @Parameter(names = {"-l", "--list"},
                   description = "list available serial ports")
        boolean list = false;

        @Parameter(names = {"-h", "--help"},
                   description = "show usage",
                   help = true)
        boolean help = false;

        static Args parse(final String... args) {
            final Args a = new Args();
            final JCommander parser = JCommander.newBuilder()
                                                .addObject(a)
                                                .programName(APP_NAME)
                                                .acceptUnknownOptions(false)
                                                .build();
            parser.parse(args);
            if (a.help)
                parser.usage();
            return a;
        }
    }

    public static final class SerialPortValidator implements IParameterValidator {
        @Override
        public void validate(final String name,
                             String value) throws ParameterException {
            value = value == null
                ? ""
                : value.substring(value.lastIndexOf('/') + 1);
            for (final SerialPort port : SerialPort.getCommPorts())
                if (port.getSystemPortName().startsWith(value) ||
                    port.getSystemPortName().endsWith(value))
                    return;
            throw new ParameterException("serial port not found: " + value);
        }
    }

    public static final class SerialPortConverter implements IStringConverter<SerialPort> {
        @Override
        public SerialPort convert(String value) {
            value = value == null
                ? ""
                : value.substring(value.lastIndexOf('/') + 1);
            for (final SerialPort port : SerialPort.getCommPorts())
                if (port.getSystemPortName().startsWith(value) ||
                    port.getSystemPortName().endsWith(value))
                    return port;
            throw new IllegalStateException("could not find serial port");
        }
    }

}
