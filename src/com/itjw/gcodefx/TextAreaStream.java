/*******************************************************************************
 * Copyright (c) 2016, Eyck Jentzsch and others
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of GCodeFXViewer nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/

package com.itjw.gcodefx;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

/**
 *
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
public class TextAreaStream extends ByteArrayOutputStream {

	private class GuiHandler extends ConsoleHandler {

		public GuiHandler(OutputStream stream) {
			setOutputStream(stream);
            setFormatter(new Formatter() {
                @Override
                public String format(LogRecord record) {
                    String throwable = "";
                    if (record.getThrown() != null) {
                        StringWriter sw = new StringWriter();
                        PrintWriter pw = new PrintWriter(sw);
                        pw.println();
                        record.getThrown().printStackTrace(pw);
                        pw.close();
                        throwable = sw.toString();
                    }
                    return String.format("%1$-16s %2$s%3$s%n", record.getLevel().toString()+":", formatMessage(record), throwable);
                }
            });

		}

	}

	public Handler getLogHandler(){
		return new GuiHandler(this);
	}

	private final List<TextArea> views = new ArrayList<TextArea>();

	private final List<OutputFilter> filters = new ArrayList<>();

	public TextAreaStream(TextArea... views) {
		super();
		this.views.clear();
		for (TextArea textArea : views) addView(textArea);
	}

	public final void addView(TextArea view) {
		views.add(view);
		filters.add((OutputFilter) (String s) -> { return true; });
	}

	public void setFilter(TextArea view, OutputFilter filter) {
		int i = views.indexOf(view);
		filters.set(i, filter);
	}

	@Override
	public synchronized void write(byte[] buf, int off, int len) {
		invokeAndWait(() -> {
			int i = 0;
			for (TextArea view : views) {
				String s = new String(buf, off, len);
				if (filters.get(i).onMatch(s)) {
					int startOffSet = view.getText().length();
					view.insertText(startOffSet, s);
				}
				i++;
			}
		});
	}

	private static void invokeAndWait(Runnable r) {
		if (Platform.isFxApplicationThread()) {
			r.run();
		} else {
			FutureTask<Boolean> task = new FutureTask<>(r, true);
			Platform.runLater(task);
			try {
				task.get(); // like join()
			} catch (InterruptedException | ExecutionException ex) {
				Logger.getLogger(TextAreaStream.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

}
