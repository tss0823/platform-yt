package com.yuntao.platform.common.log.task;

import ch.qos.logback.core.recovery.ResilientFileOutputStream;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.util.FileUtil;

import java.io.File;
import java.io.IOException;

/**
 * Created by shengshan.tang on 9/14/2015 at 5:43 PM
 */
public class HbTaskRollingFileAppender extends RollingFileAppender {

    @Override
    public void openFile(String file_name) throws IOException {
        lock.lock();
        try {
            File file = new File(file_name);
            if (FileUtil.isParentDirectoryCreationRequired(file)) {
                boolean result = FileUtil.createMissingParentDirectories(file);
                if (!result) {
                    addError("Failed to create parent directories for ["
                            + file.getAbsolutePath() + "]");
                }
            }

            ResilientFileOutputStream resilientFos = new HbTaskFileOutputStream(
                    file, append);
            resilientFos.setContext(context);
            setOutputStream(resilientFos);
        } finally {
            lock.unlock();
        }
    }

}
