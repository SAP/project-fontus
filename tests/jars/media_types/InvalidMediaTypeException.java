@SuppressWarnings("serial")
public class InvalidMediaTypeException extends IllegalArgumentException {

	private final String mediaType;

	public InvalidMediaTypeException(String mediaType, String message) {
		super("Invalid media type \"" + mediaType + "\": " + message);
		this.mediaType = mediaType;
	}
	InvalidMediaTypeException(InvalidMimeTypeException ex) {
		super(ex.getMessage(), ex);
		this.mediaType = ex.getMimeType();
	}

	public String getMediaType() {
		return this.mediaType;
	}

}

