package controllers;

import java.io.InputStream;

import play.Play;
import play.api.mvc.Result;

import com.vedantu.commons.VedantuException;
import com.vedantu.commons.utils.ContentTypeMapper;
import com.vedantu.commons.utils.OptionValue;
import com.vedantu.commons.utils.ShellExecutor;

public class Tncs extends AbstractVedantuController {

    static class  PythonHTMLDiff extends ShellExecutor {

        public PythonHTMLDiff(String cmd) {

            super(cmd);
        }

        public void setFilesForDiff(String file1, String file2) {

            OptionValue f1 = new OptionValue();
            f1.option = "-i";
            f1.value = file1;
            OptionValue f2 = new OptionValue();
            f2.option = "-i";
            f2.value = file1;

            options.add(f1);
            options.add(f2);

        }

        public InputStream getResult(String file1, String file2) throws VedantuException {

            execute();
            return this.executionStream;
        }

    }

    public static Result get() {

        String tnpaths = Play.application().configuration().getString("tnc.path");
        String test1 = "test1.html";
        String test2 = "test2.html";

        String path1 = tnpaths + "/" + test1;
        String path2 = tnpaths + "/" + test2;

      
        try {
            PythonHTMLDiff executor = new PythonHTMLDiff("python diff.py");
            executor.setMonitorable(false);
            return (Result) ok(executor.getResult(test1, test2)).as(
                    ContentTypeMapper.get().getContentType(test1));
        } catch (VedantuException e) {
            return (Result) ok(getErrorResponse(e).toObjectNode());
        }

    }
}
