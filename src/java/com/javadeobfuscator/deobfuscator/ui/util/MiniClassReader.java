// ASM: a very small and fast Java bytecode manipulation framework
// Copyright (c) 2000-2011 INRIA, France Telecom
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met:
// 1. Redistributions of source code must retain the above copyright
//    notice, this list of conditions and the following disclaimer.
// 2. Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.
// 3. Neither the name of the copyright holders nor the names of its
//    contributors may be used to endorse or promote products derived from
//    this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
// LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
// THE POSSIBILITY OF SUCH DAMAGE.
package com.javadeobfuscator.deobfuscator.ui.util;

/**
 * Minified version of ClassReader from ASM, only allows for fetching of class name. <br> Minification / stripping used because the rest of the class is unused.
 */
public class MiniClassReader
{
	private static final int CONSTANT_CLASS_TAG = 7;
	private static final int CONSTANT_FIELDREF_TAG = 9;
	private static final int CONSTANT_METHODREF_TAG = 10;
	private static final int CONSTANT_INTERFACE_METHODREF_TAG = 11;
	private static final int CONSTANT_STRING_TAG = 8;
	private static final int CONSTANT_INTEGER_TAG = 3;
	private static final int CONSTANT_FLOAT_TAG = 4;
	private static final int CONSTANT_LONG_TAG = 5;
	private static final int CONSTANT_DOUBLE_TAG = 6;
	private static final int CONSTANT_NAME_AND_TYPE_TAG = 12;
	private static final int CONSTANT_UTF8_TAG = 1;
	private static final int CONSTANT_METHOD_HANDLE_TAG = 15;
	private static final int CONSTANT_METHOD_TYPE_TAG = 16;
	private static final int CONSTANT_DYNAMIC_TAG = 17;
	private static final int CONSTANT_INVOKE_DYNAMIC_TAG = 18;
	private static final int CONSTANT_MODULE_TAG = 19;
	private static final int CONSTANT_PACKAGE_TAG = 20;
	/// ==========================================================
	private final byte[] b;
	private final int header;
	private int maxLen = 0;
	private final int[] offsets;

	public MiniClassReader(final byte[] buff)
	{
		this.b = buff;
		if (readShort(6) > 53)
		{
			throw new IllegalArgumentException("Unsupported class file major version " + readShort(6));
		}
		int poolSize = readUnsignedShort(8);
		int currIndex = 1;
		int curOffset = 10;
		offsets = new int[poolSize];
		while (currIndex < poolSize)
		{
			offsets[currIndex++] = curOffset + 1;
			int cpInfoSize;
			switch (buff[curOffset])
			{
				case CONSTANT_FIELDREF_TAG:
				case CONSTANT_METHODREF_TAG:
				case CONSTANT_INTERFACE_METHODREF_TAG:
				case CONSTANT_INTEGER_TAG:
				case CONSTANT_FLOAT_TAG:
				case CONSTANT_NAME_AND_TYPE_TAG:
				case CONSTANT_INVOKE_DYNAMIC_TAG:
				case CONSTANT_DYNAMIC_TAG:
					cpInfoSize = 5;
					break;
				case CONSTANT_LONG_TAG:
				case CONSTANT_DOUBLE_TAG:
					cpInfoSize = 9;
					currIndex++;
					break;
				case CONSTANT_UTF8_TAG:
					cpInfoSize = 3 + readUnsignedShort(curOffset + 1);
					if (cpInfoSize > maxLen)
					{
						maxLen = cpInfoSize;
					}
					break;
				case CONSTANT_METHOD_HANDLE_TAG:
					cpInfoSize = 4;
					break;
				case CONSTANT_CLASS_TAG:
				case CONSTANT_STRING_TAG:
				case CONSTANT_METHOD_TYPE_TAG:
				case CONSTANT_PACKAGE_TAG:
				case CONSTANT_MODULE_TAG:
					cpInfoSize = 3;
					break;
				default:
					throw new IllegalArgumentException();
			}
			curOffset += cpInfoSize;
		}
		this.header = curOffset;
	}

	public final String getClassName()
	{
		return readUTF8(offsets[readUnsignedShort(header + 2)], new char[maxLen]);
	}

	private final String readUTF8(final int offset, final char[] buf)
	{
		int poolIndex = readUnsignedShort(offset);
		if (offset == 0 || poolIndex == 0)
		{
			return null;
		}
		int utfOff = offsets[poolIndex];
		return readUTF(utfOff + 2, readUnsignedShort(utfOff), buf);
	}

	private final String readUTF(final int off, final int len, final char[] buf)
	{
		int currOff = off;
		int endOff = currOff + len;
		int curLen = 0;
		while (currOff < endOff)
		{
			int cB = b[currOff++];
			if ((cB & 0x80) == 0)
			{
				buf[curLen++] = (char) (cB & 0x7F);
			} else if ((cB & 0xE0) == 0xC0)
			{
				buf[curLen++] = (char) (((cB & 0x1F) << 6) + (b[currOff++] & 0x3F));
			} else
			{
				buf[curLen++] = (char) (((cB & 0xF) << 12) + ((b[currOff++] & 0x3F) << 6) + (b[currOff++] & 0x3F));
			}
		}
		return new String(buf, 0, curLen);
	}

	private final int readUnsignedShort(final int offset)
	{
		return ((b[offset] & 0xFF) << 8) | (b[offset + 1] & 0xFF);
	}

	private final short readShort(final int offset)
	{
		return (short) (((b[offset] & 0xFF) << 8) | (b[offset + 1] & 0xFF));
	}
}
