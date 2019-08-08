import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
public class MediaType extends MimeType implements Serializable {

	private static final long serialVersionUID = 2069937152339670231L;
	public static final MediaType ALL;

	public static final String ALL_VALUE = "*/*";

	public static final MediaType APPLICATION_ATOM_XML;

	public static final String APPLICATION_ATOM_XML_VALUE = "application/atom+xml";

	public static final MediaType APPLICATION_FORM_URLENCODED;

	public static final String APPLICATION_FORM_URLENCODED_VALUE = "application/x-www-form-urlencoded";

	public static final MediaType APPLICATION_JSON;

	public static final String APPLICATION_JSON_VALUE = "application/json";

	public static final MediaType APPLICATION_JSON_UTF8;

	public static final String APPLICATION_JSON_UTF8_VALUE = "application/json;charset=UTF-8";
	public static final MediaType APPLICATION_OCTET_STREAM;
	public static final String APPLICATION_OCTET_STREAM_VALUE = "application/octet-stream";

	public static final MediaType APPLICATION_PDF;

	public static final String APPLICATION_PDF_VALUE = "application/pdf";

	public static final MediaType APPLICATION_PROBLEM_JSON;

	public static final String APPLICATION_PROBLEM_JSON_VALUE = "application/problem+json";

	public static final MediaType APPLICATION_PROBLEM_JSON_UTF8;

	public static final String APPLICATION_PROBLEM_JSON_UTF8_VALUE = "application/problem+json;charset=UTF-8";

	public static final MediaType APPLICATION_PROBLEM_XML;

	public static final String APPLICATION_PROBLEM_XML_VALUE = "application/problem+xml";

	public static final MediaType APPLICATION_RSS_XML;

	public static final String APPLICATION_RSS_XML_VALUE = "application/rss+xml";

	public static final MediaType APPLICATION_STREAM_JSON;

	public static final String APPLICATION_STREAM_JSON_VALUE = "application/stream+json";

	public static final MediaType APPLICATION_XHTML_XML;

	public static final String APPLICATION_XHTML_XML_VALUE = "application/xhtml+xml";

	public static final MediaType APPLICATION_XML;

	public static final String APPLICATION_XML_VALUE = "application/xml";

	public static final MediaType IMAGE_GIF;

	public static final String IMAGE_GIF_VALUE = "image/gif";

	public static final MediaType IMAGE_JPEG;

	public static final String IMAGE_JPEG_VALUE = "image/jpeg";

	public static final MediaType IMAGE_PNG;

	public static final String IMAGE_PNG_VALUE = "image/png";

	public static final MediaType MULTIPART_FORM_DATA;

	public static final String MULTIPART_FORM_DATA_VALUE = "multipart/form-data";

	public static final MediaType TEXT_EVENT_STREAM;

	public static final String TEXT_EVENT_STREAM_VALUE = "text/event-stream";

	public static final MediaType TEXT_HTML;

	public static final String TEXT_HTML_VALUE = "text/html";

	public static final MediaType TEXT_MARKDOWN;

	public static final String TEXT_MARKDOWN_VALUE = "text/markdown";

	public static final MediaType TEXT_PLAIN;

	public static final String TEXT_PLAIN_VALUE = "text/plain";

	public static final MediaType TEXT_XML;

	public static final String TEXT_XML_VALUE = "text/xml";

	private static final String PARAM_QUALITY_FACTOR = "q";


	static {
		ALL = valueOf(ALL_VALUE);
		APPLICATION_ATOM_XML = valueOf(APPLICATION_ATOM_XML_VALUE);
		APPLICATION_FORM_URLENCODED = valueOf(APPLICATION_FORM_URLENCODED_VALUE);
		APPLICATION_JSON = valueOf(APPLICATION_JSON_VALUE);
		APPLICATION_JSON_UTF8 = valueOf(APPLICATION_JSON_UTF8_VALUE);
		APPLICATION_OCTET_STREAM = valueOf(APPLICATION_OCTET_STREAM_VALUE);
		APPLICATION_PDF = valueOf(APPLICATION_PDF_VALUE);
		APPLICATION_PROBLEM_JSON = valueOf(APPLICATION_PROBLEM_JSON_VALUE);
		APPLICATION_PROBLEM_JSON_UTF8 = valueOf(APPLICATION_PROBLEM_JSON_UTF8_VALUE);
		APPLICATION_PROBLEM_XML = valueOf(APPLICATION_PROBLEM_XML_VALUE);
		APPLICATION_RSS_XML = valueOf(APPLICATION_RSS_XML_VALUE);
		APPLICATION_STREAM_JSON = valueOf(APPLICATION_STREAM_JSON_VALUE);
		APPLICATION_XHTML_XML = valueOf(APPLICATION_XHTML_XML_VALUE);
		APPLICATION_XML = valueOf(APPLICATION_XML_VALUE);
		IMAGE_GIF = valueOf(IMAGE_GIF_VALUE);
		IMAGE_JPEG = valueOf(IMAGE_JPEG_VALUE);
		IMAGE_PNG = valueOf(IMAGE_PNG_VALUE);
		MULTIPART_FORM_DATA = valueOf(MULTIPART_FORM_DATA_VALUE);
		TEXT_EVENT_STREAM = valueOf(TEXT_EVENT_STREAM_VALUE);
		TEXT_HTML = valueOf(TEXT_HTML_VALUE);
		TEXT_MARKDOWN = valueOf(TEXT_MARKDOWN_VALUE);
		TEXT_PLAIN = valueOf(TEXT_PLAIN_VALUE);
		TEXT_XML = valueOf(TEXT_XML_VALUE);
	}

	public MediaType(String type) {
		super(type);
	}

	public MediaType(String type, String subtype) {
		super(type, subtype, Collections.emptyMap());
	}

	public MediaType(String type, String subtype, Charset charset) {
		super(type, subtype, charset);
	}

	public MediaType(String type, String subtype, double qualityValue) {
		this(type, subtype, Collections.singletonMap(PARAM_QUALITY_FACTOR, Double.toString(qualityValue)));
	}

	public MediaType(MediaType other, Charset charset) {
		super(other, charset);
	}

	public MediaType(MediaType other,  Map<String, String> parameters) {
		super(other.getType(), other.getSubtype(), parameters);
	}

	public MediaType(String type, String subtype,  Map<String, String> parameters) {
		super(type, subtype, parameters);
	}


	@Override
	protected void checkParameters(String attribute, String value) {
		super.checkParameters(attribute, value);
		if (PARAM_QUALITY_FACTOR.equals(attribute)) {
			value = unquote(value);
			double d = Double.parseDouble(value);
			//Assert.isTrue(d >= 0D && d <= 1D,
			//		"Invalid quality value \"" + value + "\": should be between 0.0 and 1.0");
		}
	}

	public double getQualityValue() {
		String qualityFactor = getParameter(PARAM_QUALITY_FACTOR);
		return (qualityFactor != null ? Double.parseDouble(unquote(qualityFactor)) : 1D);
	}

	public boolean includes( MediaType other) {
		return super.includes(other);
	}

	public boolean isCompatibleWith( MediaType other) {
		return super.isCompatibleWith(other);
	}

	public MediaType copyQualityValue(MediaType mediaType) {
		if (!mediaType.getParameters().containsKey(PARAM_QUALITY_FACTOR)) {
			return this;
		}
		Map<String, String> params = new LinkedHashMap<>(getParameters());
		params.put(PARAM_QUALITY_FACTOR, mediaType.getParameters().get(PARAM_QUALITY_FACTOR));
		return new MediaType(this, params);
	}

	public MediaType removeQualityValue() {
		if (!getParameters().containsKey(PARAM_QUALITY_FACTOR)) {
			return this;
		}
		Map<String, String> params = new LinkedHashMap<>(getParameters());
		params.remove(PARAM_QUALITY_FACTOR);
		return new MediaType(this, params);
	}

	public static MediaType valueOf(String value) {
		return parseMediaType(value);
	}

	public static MediaType parseMediaType(String mediaType) {
		MimeType type;
		try {
			type = MimeTypeUtils.parseMimeType(mediaType);
		}
		catch (InvalidMimeTypeException ex) {
			throw new InvalidMediaTypeException(ex);
		}
		try {
			return new MediaType(type.getType(), type.getSubtype(), type.getParameters());
		}
		catch (IllegalArgumentException ex) {
			throw new InvalidMediaTypeException(mediaType, ex.getMessage());
		}
	}

	public static List<MediaType> parseMediaTypes( String mediaTypes) {
		if (!StringUtils.hasLength(mediaTypes)) {
			return Collections.emptyList();
		}
		return MimeTypeUtils.tokenize(mediaTypes).stream()
				.filter(StringUtils::hasText)
				.map(MediaType::parseMediaType)
				.collect(Collectors.toList());
	}

	public static List<MediaType> parseMediaTypes( List<String> mediaTypes) {
		if (CollectionUtils.isEmpty(mediaTypes)) {
			return Collections.emptyList();
		}
		else if (mediaTypes.size() == 1) {
			return parseMediaTypes(mediaTypes.get(0));
		}
		else {
			List<MediaType> result = new ArrayList<>(8);
			for (String mediaType : mediaTypes) {
				result.addAll(parseMediaTypes(mediaType));
			}
			return result;
		}
	}

	public static List<MediaType> asMediaTypes(List<MimeType> mimeTypes) {
		return mimeTypes.stream().map(MediaType::asMediaType).collect(Collectors.toList());
	}

	public static MediaType asMediaType(MimeType mimeType) {
		if (mimeType instanceof MediaType) {
			return (MediaType) mimeType;
		}
		return new MediaType(mimeType.getType(), mimeType.getSubtype(), mimeType.getParameters());
	}

	public static String toString(Collection<MediaType> mediaTypes) {
		return MimeTypeUtils.toString(mediaTypes);
	}

	public static void sortBySpecificity(List<MediaType> mediaTypes) {
		if (mediaTypes.size() > 1) {
			mediaTypes.sort(SPECIFICITY_COMPARATOR);
		}
	}
	public static void sortByQualityValue(List<MediaType> mediaTypes) {
		if (mediaTypes.size() > 1) {
			mediaTypes.sort(QUALITY_VALUE_COMPARATOR);
		}
	}

	public static void sortBySpecificityAndQuality(List<MediaType> mediaTypes) {
		if (mediaTypes.size() > 1) {
			mediaTypes.sort(MediaType.SPECIFICITY_COMPARATOR.thenComparing(MediaType.QUALITY_VALUE_COMPARATOR));
		}
	}
	public static final Comparator<MediaType> QUALITY_VALUE_COMPARATOR = (mediaType1, mediaType2) -> {
		double quality1 = mediaType1.getQualityValue();
		double quality2 = mediaType2.getQualityValue();
		int qualityComparison = Double.compare(quality2, quality1);
		if (qualityComparison != 0) {
			return qualityComparison;  // audio/*;q=0.7 < audio/*;q=0.3
		}
		else if (mediaType1.isWildcardType() && !mediaType2.isWildcardType()) {  // */* < audio/*
			return 1;
		}
		else if (mediaType2.isWildcardType() && !mediaType1.isWildcardType()) {  // audio/* > */*
			return -1;
		}
		else if (!mediaType1.getType().equals(mediaType2.getType())) {  // audio/basic == text/html
			return 0;
		}
		else {  // mediaType1.getType().equals(mediaType2.getType())
			if (mediaType1.isWildcardSubtype() && !mediaType2.isWildcardSubtype()) {  // audio/* < audio/basic
				return 1;
			}
			else if (mediaType2.isWildcardSubtype() && !mediaType1.isWildcardSubtype()) {  // audio/basic > audio/*
				return -1;
			}
			else if (!mediaType1.getSubtype().equals(mediaType2.getSubtype())) {  // audio/basic == audio/wave
				return 0;
			}
			else {
				int paramsSize1 = mediaType1.getParameters().size();
				int paramsSize2 = mediaType2.getParameters().size();
				return Integer.compare(paramsSize2, paramsSize1);  // audio/basic;level=1 < audio/basic
			}
		}
	};


	public static final Comparator<MediaType> SPECIFICITY_COMPARATOR = new SpecificityComparator<MediaType>() {

		@Override
		protected int compareParameters(MediaType mediaType1, MediaType mediaType2) {
			double quality1 = mediaType1.getQualityValue();
			double quality2 = mediaType2.getQualityValue();
			int qualityComparison = Double.compare(quality2, quality1);
			if (qualityComparison != 0) {
				return qualityComparison;  // audio/*;q=0.7 < audio/*;q=0.3
			}
			return super.compareParameters(mediaType1, mediaType2);
		}
	};

}
