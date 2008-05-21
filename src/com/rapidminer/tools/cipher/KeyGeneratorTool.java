/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2008 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package com.rapidminer.tools.cipher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.rapidminer.tools.LogService;
import com.rapidminer.tools.ParameterService;

/**
 * This class can be used to generate a new key and store it in the user
 * directory. Please note that existing keys will be overwritten
 * by objects of this class. That means that passwords stored with &quot;old&quot;
 * keys can no longer be decrypted.
 *
 * @author Ingo Mierswa
 * @version $Id: KeyGeneratorTool.java,v 1.5 2008/05/09 19:23:26 ingomierswa Exp $
 */
public class KeyGeneratorTool {
	
	private static final String GENERATOR_TYPE = "DESede";
	
	private static final String KEY_FILE_NAME = "cipher.key";
	
	public static void createAndStoreKey() throws KeyGenerationException {
		KeyGenerator keyGenerator = null;
		try {
			keyGenerator = KeyGenerator.getInstance(GENERATOR_TYPE);
		} catch (NoSuchAlgorithmException e) {
			throw new KeyGenerationException("Cannot generate key, generation algorithm not known.");
		}
		
		if (keyGenerator != null) {
			keyGenerator.init(168, new SecureRandom());
			
			// actual generation
			SecretKey key = keyGenerator.generateKey();
			
			File keyFile = new File(ParameterService.getUserRapidMinerDir(), KEY_FILE_NAME);
			boolean result = keyFile.delete();
			if (!result)
				LogService.getGlobal().logError("Cannot delete old key file.");
			
			byte[] rawKey = key.getEncoded();
			
	        try {
	        	ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(keyFile));
	        	out.writeInt(rawKey.length);
	        	out.write(rawKey);
	        	out.close();
	        } catch (Exception e) {
	        	e.printStackTrace();
	        	throw new KeyGenerationException("Cannot store key: " + e.getMessage());
	        }
		}
	}
	
	public static Key getUserKey() throws IOException {
		File keyFile = new File(ParameterService.getUserRapidMinerDir(), KEY_FILE_NAME);
		ObjectInputStream in = null;
		try {
			in = new ObjectInputStream(new FileInputStream(keyFile));
			int length = in.readInt();
			byte[] rawKey = new byte[length];
			int actualLength = in.read(rawKey);
			if (length != actualLength)
				throw new IOException("Cannot read key file (unexpected length)");
			return new SecretKeySpec(rawKey, GENERATOR_TYPE);
		} catch (Exception e) {
			throw new IOException("Cannot retrieve key: " + e.getMessage());
		} finally {
			if (in != null)
				in.close();
		}
	}
}
