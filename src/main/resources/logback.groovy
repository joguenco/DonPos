import static ch.qos.logback.classic.Level.DEBUG
import static ch.qos.logback.classic.Level.INFO
import static ch.qos.logback.classic.Level.ALL

import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy

import java.nio.charset.Charset

def patternString = '%d{YYYY-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg %ex{20}%n'
def userHome = System.getProperty("user.home")

appender("applicationLogFile", RollingFileAppender) {
  rollingPolicy(TimeBasedRollingPolicy) {
    fileNamePattern = "$userHome/.donpos/donpos-%d{yyyy-MM-dd}.log"
    maxHistory = "5"
  }
  encoder(PatternLayoutEncoder) {
    charset = Charset.forName("UTF-8")
    pattern = patternString
  }
}

appender('console', ConsoleAppender) {
  encoder(PatternLayoutEncoder) {
    pattern = patternString
  }
}

logger('com.unicenta', INFO)

root(INFO, ['console', 'applicationLogFile'])
