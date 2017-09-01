package io.github.thistestuser;

import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JTextArea;

public class TextOutputStream extends OutputStream
{
	private JTextArea textArea;
	
	public TextOutputStream(JTextArea textArea)
	{
		this.textArea = textArea;
		textArea.setEditable(false);
	}
	
	@Override
	public void flush()
	{}
	
	@Override
	public void close()
	{}
	
	@Override
	public void write(int b) throws IOException
	{
		textArea.append(String.valueOf((char)b));
	}
}
