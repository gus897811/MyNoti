from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "icon(1250x1250).png"
RES = ROOT / "app" / "src" / "main" / "res"


def content_bbox(image: Image.Image, white_threshold: int = 245) -> tuple[int, int, int, int]:
    pixels = image.load()
    width, height = image.size
    min_x, min_y, max_x, max_y = width, height, 0, 0
    found = False
    for y in range(height):
        for x in range(width):
            r, g, b, a = pixels[x, y]
            if a > 10 and not (r > white_threshold and g > white_threshold and b > white_threshold):
                found = True
                min_x = min(min_x, x)
                min_y = min(min_y, y)
                max_x = max(max_x, x)
                max_y = max(max_y, y)
    if not found:
        raise RuntimeError("No non-white content found")
    return min_x, min_y, max_x + 1, max_y + 1


def crop_with_padding(image: Image.Image, bbox: tuple[int, int, int, int], padding: int) -> Image.Image:
    left, top, right, bottom = bbox
    left = max(0, left - padding)
    top = max(0, top - padding)
    right = min(image.width, right + padding)
    bottom = min(image.height, bottom + padding)
    return image.crop((left, top, right, bottom))


def fit_on_square(image: Image.Image, size: int, scale: float, background=(255, 255, 255, 255)) -> Image.Image:
    canvas = Image.new("RGBA", (size, size), background)
    max_w = int(size * scale)
    max_h = int(size * scale)
    ratio = min(max_w / image.width, max_h / image.height)
    new_size = (max(1, int(image.width * ratio)), max(1, int(image.height * ratio)))
    fitted = image.resize(new_size, Image.Resampling.LANCZOS)
    x = (size - fitted.width) // 2
    y = (size - fitted.height) // 2
    canvas.alpha_composite(fitted, (x, y))
    return canvas


def make_white_transparent(image: Image.Image, white_threshold: int = 245) -> Image.Image:
    pixels = image.load()
    result = image.copy()
    out = result.load()
    for y in range(image.height):
        for x in range(image.width):
            r, g, b, a = pixels[x, y]
            if a > 10 and r > white_threshold and g > white_threshold and b > white_threshold:
                out[x, y] = (255, 255, 255, 0)
    return result


def to_monochrome(image: Image.Image) -> Image.Image:
    pixels = image.load()
    result = Image.new("RGBA", image.size, (0, 0, 0, 0))
    out = result.load()
    for y in range(image.height):
        for x in range(image.width):
            r, g, b, a = pixels[x, y]
            if a > 10 and not (r > 245 and g > 245 and b > 245):
                out[x, y] = (0, 0, 0, 255)
    return result


def save_png(image: Image.Image, path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    image.save(path, format="PNG")


def main() -> None:
    source = Image.open(SOURCE).convert("RGBA")
    bbox = content_bbox(source)
    print("bbox", bbox, "w", bbox[2] - bbox[0], "h", bbox[3] - bbox[1])

    wordmark = crop_with_padding(source, bbox, padding=24)
    print("wordmark", wordmark.size)

    save_png(make_white_transparent(wordmark), RES / "drawable-xxhdpi" / "ic_mynoti_logo.png")

    foreground = fit_on_square(wordmark, size=432, scale=0.62)
    save_png(foreground, RES / "drawable-xxxhdpi" / "ic_launcher_foreground.png")
    save_png(to_monochrome(foreground), RES / "drawable-xxxhdpi" / "ic_launcher_monochrome.png")

    launcher_sizes = {
        "mipmap-mdpi": 48,
        "mipmap-hdpi": 72,
        "mipmap-xhdpi": 96,
        "mipmap-xxhdpi": 144,
        "mipmap-xxxhdpi": 192,
    }
    for folder, size in launcher_sizes.items():
        launcher = fit_on_square(wordmark, size=size, scale=0.78)
        folder_path = RES / folder
        save_png(launcher, folder_path / "ic_launcher.png")
        save_png(launcher, folder_path / "ic_launcher_round.png")
        for stale in ("ic_launcher.webp", "ic_launcher_round.webp"):
            stale_path = folder_path / stale
            if stale_path.exists():
                stale_path.unlink()


if __name__ == "__main__":
    main()
