package com.kodedu.terminalfx;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kodedu.terminalfx.annotation.WebkitCall;
import com.kodedu.terminalfx.config.TerminalConfig;
import com.kodedu.terminalfx.helper.ThreadHelper;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

import java.io.Reader;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

//import java.nio.charset.*;
//import java.nio.*;
//
//import javafx.beans.value.ChangeListener;
//import javafx.beans.value.ObservableValue;
//import javafx.concurrent.Worker;

public class TerminalView extends Pane {

	private final WebView webView;
	private final ReadOnlyIntegerWrapper columnsProperty;
	private final ReadOnlyIntegerWrapper rowsProperty;
	private final ObjectProperty<Reader> inputReaderProperty;
	private final ObjectProperty<Reader> errorReaderProperty;
	private TerminalConfig terminalConfig = new TerminalConfig();
	protected final CountDownLatch countDownLatch = new CountDownLatch(1);


	public TerminalView() {
		webView = new WebView();
		columnsProperty = new ReadOnlyIntegerWrapper(150);
		rowsProperty = new ReadOnlyIntegerWrapper(10);

		inputReaderProperty = new SimpleObjectProperty<>();
		errorReaderProperty = new SimpleObjectProperty<>();

		inputReaderProperty.addListener((observable, oldValue, newValue) -> {
			ThreadHelper.start(() -> {
				System.out.println("inputReader:" + newValue);
				printReader(newValue);
			});
		});

		errorReaderProperty.addListener((observable, oldValue, newValue) -> {
			ThreadHelper.start(() -> {
				System.out.println("errorReader:" + newValue);
				printReader(newValue);
			});
		});

		// svi javascript unutar app
		webView.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
			getWindow().setMember("console", this);
		});

		webView.prefHeightProperty().bind(heightProperty());
		webView.prefWidthProperty().bind(widthProperty());

		// webView.getEngine().setJavaScriptEnabled(true);
		//WebEnginWebEngine webEngine = webView.getEngine();


		// toExternalForm() returns String representation of java.net.URL object:

		String url = TerminalView.class.getResource("/hello.html").toExternalForm();
		System.out.println("url: " + url);

		webEngine().load(url);

		// webEngine.load(TerminalView.class.getResource("/xterm.html").toExternalForm());
		// webEngine.load("https://www.google.ba");

		// hernad
		//getChildren().add(webView);

	}

	@WebkitCall(from = "hello") // hello.html
	public String getPrefs() {
		System.out.println("JAVA: getPrefs");
		try {
			return new ObjectMapper().writeValueAsString(getTerminalConfig());
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void updatePrefs(TerminalConfig terminalConfig) {
		if (getTerminalConfig().equals(terminalConfig)) {
			return;
		}

		setTerminalConfig(terminalConfig);
		final String prefs = getPrefs();

		ThreadHelper.runActionLater(() -> {
			try {
				getWindow().call("updatePrefs", prefs);
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}, true);
	}

	@WebkitCall(from = "hello")
	public void resizeTerminal(int columns, int rows) {
		System.out.println("JAVA: resizeTerminal");
		columnsProperty.set(columns);
		rowsProperty.set(rows);
	}

	@WebkitCall
	public void onTerminalInit() {
		System.out.println("java onTerminalInit");
		ThreadHelper.runActionLater(() -> {
			getChildren().add(webView);
		}, true);
	}

	@WebkitCall
	public void fromJavaScript(String msg) {
		System.out.println(msg);
	}

	@WebkitCall
	public void log(String msg) {
		System.out.println("Invoked from JavaScript: " + msg);
	}

	@WebkitCall
	/**
	 * Internal use only
	 */
	public void onTerminalReady() {
		System.out.println("java onTerminalReady");
		ThreadHelper.start(() -> {
			try {
				focusCursor();
				countDownLatch.countDown();
			} catch (final Exception e) {
			}
		});
	}

	private void printReader(Reader bufferedReader) {
		try {
			int nRead;
			final char[] bytes = new char[1 * 1024];

			while ((nRead = bufferedReader.read(bytes, 0, bytes.length)) != -1) {

				char[] readData = new char[nRead];
				System.arraycopy(bytes, 0, readData, 0, nRead);

				String str = new String(readData);
				// str += "┐─┬╵│└├┘┤┴┼šŠ";
				System.out.println("PrintReader: " + str);
				print(str);

			}

		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	@WebkitCall(from = "hello")
	public void copy(String text) {
		System.out.println("JAVA: copy");
		final Clipboard clipboard = Clipboard.getSystemClipboard();
		final ClipboardContent clipboardContent = new ClipboardContent();
		clipboardContent.putString(text);
		clipboard.setContent(clipboardContent);
	}

	public void onTerminalFxReady(Runnable onReadyAction) {
		System.out.println("JAVA: onTerminalFXReady");
		ThreadHelper.start(() -> {
			ThreadHelper.awaitLatch(countDownLatch);

			if (Objects.nonNull(onReadyAction)) {
				ThreadHelper.start(onReadyAction);
			}
		});
	}

	protected void print(String text) {
		System.out.println("JAVA: print");
		ThreadHelper.awaitLatch(countDownLatch);
		ThreadHelper.runActionLater(() -> {
			getTerminalIO().call("print", text);
		});

	}

	public void focusCursor() {
		System.out.println("JAVA: focusCursor");
		ThreadHelper.runActionLater(() -> {
			webView.requestFocus();
			getTerminal().call("focus");
		}, true);
	}

	private JSObject getTerminal() {
		return (JSObject) webEngine().executeScript("t");
	}

	private JSObject getTerminalIO() {
		return (JSObject) webEngine().executeScript("t.io");
	}

	public JSObject getWindow() {
		return (JSObject) webEngine().executeScript("window");
	}

	
	private WebEngine webEngine() {
		return webView.getEngine();
	}


	public TerminalConfig getTerminalConfig() {
		if (Objects.isNull(terminalConfig)) {
			terminalConfig = new TerminalConfig();
		}
		return terminalConfig;
	}

	public void setTerminalConfig(TerminalConfig terminalConfig) {
		this.terminalConfig = terminalConfig;
	}

	public ReadOnlyIntegerProperty columnsProperty() {
		return columnsProperty.getReadOnlyProperty();
	}

	public int getColumns() {
		System.out.println("getColumns" + String.valueOf(columnsProperty.get()));
		return columnsProperty.get();
	}

	public ReadOnlyIntegerProperty rowsProperty() {
		return rowsProperty.getReadOnlyProperty();
	}

	public int getRows() {
		System.out.println("getRows" + String.valueOf(rowsProperty.get()));
		return rowsProperty.get();
	}

	public ObjectProperty<Reader> inputReaderProperty() {
		return inputReaderProperty;
	}

	public Reader getInputReader() {
		return inputReaderProperty.get();
	}

	public void setInputReader(Reader reader) {
		inputReaderProperty.set(reader);
	}

	public ObjectProperty<Reader> errorReaderProperty() {
		return errorReaderProperty;
	}

	public Reader getErrorReader() {
		return errorReaderProperty.get();
	}

	public void setErrorReader(Reader reader) {
		errorReaderProperty.set(reader);
	}

}
