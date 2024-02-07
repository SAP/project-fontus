import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * A ZIP File "Central directory file header record" (CDFH).
 *
 * @author Phillip Webb
 * @author Andy Wilkinson
 * @see <a href="https://en.wikipedia.org/wiki/Zip_%28file_format%29">Zip File Format</a>
 */

final class CentralDirectoryFileHeader implements FileHeader {

	private static final AsciiBytes SLASH = new AsciiBytes("/");

	private static final byte[] NO_EXTRA = {};

	private static final AsciiBytes NO_COMMENT = new AsciiBytes("");

	private byte[] header;

	private int headerOffset;

	private AsciiBytes name;

	private byte[] extra;

	private AsciiBytes comment;

	private long localHeaderOffset;

	CentralDirectoryFileHeader() {
	}

	CentralDirectoryFileHeader(byte[] header, int headerOffset, AsciiBytes name, byte[] extra, AsciiBytes comment,
			long localHeaderOffset) {
		this.header = header;
		this.headerOffset = headerOffset;
		this.name = name;
		this.extra = extra;
		this.comment = comment;
		this.localHeaderOffset = localHeaderOffset;
	}


	AsciiBytes getName() {
		return this.name;
	}

	@Override
	public boolean hasName(CharSequence name, char suffix) {
		return this.name.matches(name, suffix);
	}

	boolean isDirectory() {
		return this.name.endsWith(SLASH);
	}

	@Override
	public int getMethod() {
		return (int) Bytes.littleEndianValue(this.header, this.headerOffset + 10, 2);
	}

	long getTime() {
		long datetime = Bytes.littleEndianValue(this.header, this.headerOffset + 12, 4);
		return decodeMsDosFormatDateTime(datetime);
	}

	/**
	 * Decode MS-DOS Date Time details. See <a href=
	 * "https://docs.microsoft.com/en-gb/windows/desktop/api/winbase/nf-winbase-dosdatetimetofiletime">
	 * Microsoft's documentation</a> for more details of the format.
	 * @param datetime the date and time
	 * @return the date and time as milliseconds since the epoch
	 */
	private long decodeMsDosFormatDateTime(long datetime) {
		LocalDateTime localDateTime = LocalDateTime.of((int) (((datetime >> 25) & 0x7f) + 1980),
				(int) ((datetime >> 21) & 0x0f), (int) ((datetime >> 16) & 0x1f), (int) ((datetime >> 11) & 0x1f),
				(int) ((datetime >> 5) & 0x3f), (int) ((datetime << 1) & 0x3e));
		return localDateTime.toEpochSecond(ZoneId.systemDefault().getRules().getOffset(localDateTime)) * 1000;
	}

	long getCrc() {
		return Bytes.littleEndianValue(this.header, this.headerOffset + 16, 4);
	}

	@Override
	public long getCompressedSize() {
		return Bytes.littleEndianValue(this.header, this.headerOffset + 20, 4);
	}

	@Override
	public long getSize() {
		return Bytes.littleEndianValue(this.header, this.headerOffset + 24, 4);
	}

	byte[] getExtra() {
		return this.extra;
	}

	boolean hasExtra() {
		return this.extra.length > 0;
	}

	AsciiBytes getComment() {
		return this.comment;
	}

	@Override
	public long getLocalHeaderOffset() {
		return this.localHeaderOffset;
	}

	@Override
	public CentralDirectoryFileHeader clone() {
		byte[] header = new byte[46];
		System.arraycopy(this.header, this.headerOffset, header, 0, header.length);
		return new CentralDirectoryFileHeader(header, 0, this.name, header, this.comment, this.localHeaderOffset);
	}

}
