import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public abstract class MimeTypeUtils {

	private static final byte[] BOUNDARY_CHARS =
			new byte[] {'-', '_', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', 'a', 'b', 'c', 'd', 'e', 'f', 'g',
					'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A',
					'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U',
					'V', 'W', 'X', 'Y', 'Z'};

	public static final Comparator<MimeType> SPECIFICITY_COMPARATOR = new MimeType.SpecificityComparator<>();

	public static final MimeType ALL;

	public static final String ALL_VALUE = "*/*";

	public static final MimeType APPLICATION_JSON;

	public static final String APPLICATION_JSON_VALUE = "application/json";

	public static final MimeType APPLICATION_OCTET_STREAM;

	public static final String APPLICATION_OCTET_STREAM_VALUE = "application/octet-stream";

	public static final MimeType APPLICATION_XML;

	public static final String APPLICATION_XML_VALUE = "application/xml";

	public static final MimeType IMAGE_GIF;

	public static final String IMAGE_GIF_VALUE = "image/gif";

	public static final MimeType IMAGE_JPEG;

	public static final String IMAGE_JPEG_VALUE = "image/jpeg";

	public static final MimeType IMAGE_PNG;

	public static final String IMAGE_PNG_VALUE = "image/png";

	public static final MimeType TEXT_HTML;

	public static final String TEXT_HTML_VALUE = "text/html";

	public static final MimeType TEXT_PLAIN;

	public static final String TEXT_PLAIN_VALUE = "text/plain";

	public static final MimeType TEXT_XML;

	public static final String TEXT_XML_VALUE = "text/xml";

	private static volatile Random random;

	static {
		ALL = MimeType.valueOf(ALL_VALUE);
		APPLICATION_JSON = MimeType.valueOf(APPLICATION_JSON_VALUE);
		APPLICATION_OCTET_STREAM = MimeType.valueOf(APPLICATION_OCTET_STREAM_VALUE);
		APPLICATION_XML = MimeType.valueOf(APPLICATION_XML_VALUE);
		IMAGE_GIF = MimeType.valueOf(IMAGE_GIF_VALUE);
		IMAGE_JPEG = MimeType.valueOf(IMAGE_JPEG_VALUE);
		IMAGE_PNG = MimeType.valueOf(IMAGE_PNG_VALUE);
		TEXT_HTML = MimeType.valueOf(TEXT_HTML_VALUE);
		TEXT_PLAIN = MimeType.valueOf(TEXT_PLAIN_VALUE);
		TEXT_XML = MimeType.valueOf(TEXT_XML_VALUE);
	}


	public static MimeType parseMimeType(String mimeType) {
		if (!StringUtils.hasLength(mimeType)) {
			throw new InvalidMimeTypeException(mimeType, "'mimeType' must not be empty");
		}

		int index = mimeType.indexOf(';');
		String fullType = (index >= 0 ? mimeType.substring(0, index) : mimeType).trim();
		if (fullType.isEmpty()) {
			throw new InvalidMimeTypeException(mimeType, "'mimeType' must not be empty");
		}

		// java.net.HttpURLConnection returns a *; q=.2 Accept header
		if (MimeType.WILDCARD_TYPE.equals(fullType)) {
			fullType = "*/*";
		}
		int subIndex = fullType.indexOf('/');
		if (subIndex == -1) {
			throw new InvalidMimeTypeException(mimeType, "does not contain '/'");
		}
		if (subIndex == fullType.length() - 1) {
			throw new InvalidMimeTypeException(mimeType, "does not contain subtype after '/'");
		}
		String type = fullType.substring(0, subIndex);
		String subtype = fullType.substring(subIndex + 1, fullType.length());
		if (MimeType.WILDCARD_TYPE.equals(type) && !MimeType.WILDCARD_TYPE.equals(subtype)) {
			throw new InvalidMimeTypeException(mimeType, "wildcard type is legal only in '*/*' (all mime types)");
		}

		Map<String, String> parameters = null;
		do {
			int nextIndex = index + 1;
			boolean quoted = false;
			while (nextIndex < mimeType.length()) {
				char ch = mimeType.charAt(nextIndex);
				if (ch == ';') {
					if (!quoted) {
						break;
					}
				}
				else if (ch == '"') {
					quoted = !quoted;
				}
				nextIndex++;
			}
			String parameter = mimeType.substring(index + 1, nextIndex).trim();
			if (parameter.length() > 0) {
				if (parameters == null) {
					parameters = new LinkedHashMap<>(4);
				}
				int eqIndex = parameter.indexOf('=');
				if (eqIndex >= 0) {
					String attribute = parameter.substring(0, eqIndex).trim();
					String value = parameter.substring(eqIndex + 1, parameter.length()).trim();
					parameters.put(attribute, value);
				}
			}
			index = nextIndex;
		}
		while (index < mimeType.length());

		try {
			return new MimeType(type, subtype, parameters);
		}
		catch (UnsupportedCharsetException ex) {
			throw new InvalidMimeTypeException(mimeType, "unsupported charset '" + ex.getCharsetName() + "'");
		}
		catch (IllegalArgumentException ex) {
			throw new InvalidMimeTypeException(mimeType, ex.getMessage());
		}
	}

	public static List<MimeType> parseMimeTypes(String mimeTypes) {
		if (!StringUtils.hasLength(mimeTypes)) {
			return Collections.emptyList();
		}
		return tokenize(mimeTypes).stream()
				.filter(StringUtils::hasText)
				.map(MimeTypeUtils::parseMimeType)
				.collect(Collectors.toList());
	}

	public static List<String> tokenize(String mimeTypes) {
		if (!StringUtils.hasLength(mimeTypes)) {
			return Collections.emptyList();
		}
		List<String> tokens = new ArrayList<>();
		boolean inQuotes = false;
		int startIndex = 0;
		int i = 0;
		while (i < mimeTypes.length()) {
			switch (mimeTypes.charAt(i)) {
				case '"':
					inQuotes = !inQuotes;
					break;
				case ',':
					if (!inQuotes) {
						tokens.add(mimeTypes.substring(startIndex, i));
						startIndex = i + 1;
					}
					break;
				case '\\':
					i++;
					break;
			}
			i++;
		}
		tokens.add(mimeTypes.substring(startIndex));
		return tokens;
	}

	public static String toString(Collection<? extends MimeType> mimeTypes) {
		StringBuilder builder = new StringBuilder();
		for (Iterator<? extends MimeType> iterator = mimeTypes.iterator(); iterator.hasNext();) {
			MimeType mimeType = iterator.next();
			mimeType.appendTo(builder);
			if (iterator.hasNext()) {
				builder.append(", ");
			}
		}
		return builder.toString();
	}

	public static void sortBySpecificity(List<MimeType> mimeTypes) {
		//Assert.notNull(mimeTypes, "'mimeTypes' must not be null");
		if (mimeTypes.size() > 1) {
			mimeTypes.sort(SPECIFICITY_COMPARATOR);
		}
	}

	private static Random initRandom() {
		Random randomToUse = random;
		if (randomToUse == null) {
			synchronized (MimeTypeUtils.class) {
				randomToUse = random;
				if (randomToUse == null) {
					randomToUse = new SecureRandom();
					random = randomToUse;
				}
			}
		}
		return randomToUse;
	}

	public static byte[] generateMultipartBoundary() {
		Random randomToUse = initRandom();
		byte[] boundary = new byte[randomToUse.nextInt(11) + 30];
		for (int i = 0; i < boundary.length; i++) {
			boundary[i] = BOUNDARY_CHARS[randomToUse.nextInt(BOUNDARY_CHARS.length)];
		}
		return boundary;
	}

	public static String generateMultipartBoundaryString() {
		return new String(generateMultipartBoundary(), StandardCharsets.US_ASCII);
	}

}

