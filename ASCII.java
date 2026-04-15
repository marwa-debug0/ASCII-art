import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class ASCII {

    // ─── 1. IMAGE LOADING ────────────────────────────────────────────────────────

    public static class ImageInfo {
        private final int width;
        private final int height;
        private final BufferedImage image;

        public ImageInfo(String imagePath) throws IOException {
            this.image  = ImageIO.read(new File(imagePath));
            if (this.image == null)
                throw new IOException("Could not read image: " + imagePath);
            this.width  = image.getWidth();
            this.height = image.getHeight();
        }

        public BufferedImage getImage()  { 
            return image;  
        }

        public int getWidth()  { 
            return width;  
        }

        public int getHeight() { 
            return height; 
        }
    }

    // ─── 2. RGB PIXEL ────────────────────────────────────────────────────────────

    public static class RGB {
        public final int red;
        public final int green;
        public final int blue;

        public RGB(int red, int green, int blue) {
            this.red   = red;
            this.green = green;
            this.blue  = blue;   
        }

        //Grayscale (ITU-R BT.601)
        public int toGray() {
            return (int) (0.299 * red + 0.587 * green + 0.114 * blue);
        }

        @Override
        public String toString() {
            return "(" + red + "," + green + "," + blue + ")";
        }
    }

    // ─── 3. RGB MATRIX ───────────────────────────────────────────────────────────

    public static class RGBMatrix {
        private final RGB[][] matrix;
        private final int     width;
        private final int     height;

        public RGBMatrix(ImageInfo imageInfo) {
            BufferedImage image = imageInfo.getImage();
            this.width  = imageInfo.getWidth();
            this.height = imageInfo.getHeight();
            matrix = new RGB[height][width];

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int pixel = image.getRGB(x, y);
                    int red   = (pixel >> 16) & 0xFF;
                    int green = (pixel >>  8) & 0xFF;
                    int blue  =  pixel        & 0xFF;
                    matrix[y][x] = new RGB(red, green, blue); 
                }
            }
        }

        public RGB getPixel(int x, int y) { return matrix[y][x]; }
        public int getWidth()             { return width;         }
        public int getHeight()            { return height;        }
    }

    // ─── 4. ASCII CONVERTER ──────────────────────────────────────────────────────

    public static class AsciiConverter {

        // Dark → light: denser chars map to darker pixels
        private static final String CHARS = "$@B%8&WM#*oahkbdpqwmZO0QLCJUYXzcvunxrjft/\\|()1{}[]?-_+~<>i!lI;:,\"^`'. ";

        /**
         * Maps a grayscale value (0–255) to an ASCII character.
         * 0 = darkest, 255 = lightest.
         */
        private static char grayToChar(int gray) {
            int index = (gray * (CHARS.length() - 1)) / 255;
            return CHARS.charAt(index);
        }

        //Convert an RGBMatrix to an ASCII string.
        public static String convert(RGBMatrix rgbMatrix) {
            int w = rgbMatrix.getWidth();
            int h = rgbMatrix.getHeight();
            StringBuilder sb = new StringBuilder(h * (w + 1));

            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int gray = rgbMatrix.getPixel(x, y).toGray();
                    sb.append(grayToChar(gray));
                }
                sb.append('\n');
            }
            return sb.toString();
        }

        //Save the ASCII art to a .txt file.
        public static void saveToFile(String asciiArt, String outputPath) throws IOException {
            try (PrintWriter pw = new PrintWriter(new FileWriter(outputPath))) {
                pw.print(asciiArt);
            }
            System.out.println("ASCII art saved to: " + outputPath);
        }
    }

    // ─── 5. MAIN ─────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        // ── Configuration ──────────────────────────────────────────────────────
        String inputPath  = args.length > 0 ? args[0] : "/Users/marwa/Downloads/Kiity.jpg";
        String outputPath = args.length > 1 ? args[1] : "output.txt";
        

        try {
            //Load the image
            System.out.println("Loading image: " + inputPath);
            ImageInfo imageInfo = new ImageInfo(inputPath);
            System.out.printf("Image size: %dx%d%n", imageInfo.getWidth(), imageInfo.getHeight());

            //Build RGB matrix
            RGBMatrix rgbMatrix = new RGBMatrix(imageInfo);
            System.out.println("RGB matrix built successfully.");

            //Convert to ASCII
            String asciiArt = AsciiConverter.convert(rgbMatrix);

            //Print to console
            System.out.println("\n── ASCII Art ─────────────────────────────────────────\n");
            System.out.println(asciiArt);

            //Save to file
            AsciiConverter.saveToFile(asciiArt, outputPath);

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}