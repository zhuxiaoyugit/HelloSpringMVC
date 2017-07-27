package com.xrun.user.util;

import java.util.Properties;

import kafka.serializer.Encoder;
import kafka.utils.VerifiableProperties;

/**
 * @author Soby Chacko
 * @author Marius Bogoevici
 * @since 0.5
 */
public class StringEncoder implements Encoder<String> {

	private kafka.serializer.StringEncoder stringEncoder;

	public StringEncoder() {
		this("UTF-8");
	}

	public StringEncoder(String encoding) {
		final Properties props = new Properties();
		props.put("serializer.encoding", encoding);
		final VerifiableProperties verifiableProperties = new VerifiableProperties(props);
		stringEncoder = new kafka.serializer.StringEncoder(verifiableProperties);
	}

	@Override
	public byte[] toBytes(final String value) {
		return stringEncoder.toBytes(value);
	}
}
