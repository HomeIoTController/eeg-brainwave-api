package config;

import org.slf4j.LoggerFactory;
import weka.core.logging.Logger;

import java.util.Date;

public class WekaLogger extends Logger {

    private final String m_LineFeed = System.getProperty("line.separator");

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(WekaLogger.class);
    @Override
    protected void doLog(Level level, String msg, String cls, String method, int lineno) {
        switch (level) {
            case ALL:
            case INFO:
                log.info(m_DateFormat.format(new Date()) + " " + cls + " " + method + m_LineFeed + level + ": " + msg + m_LineFeed);
                break;
            case SEVERE:
            case WARNING:
                log.warn(m_DateFormat.format(new Date()) + " " + cls + " " + method + m_LineFeed + level + ": " + msg + m_LineFeed);
                break;
            case OFF:
                break;
            default:
                break;
        }
    }

    @Override
    public String getRevision() {
        return null;
    }
}
