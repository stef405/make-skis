from PIL import Image, ImageDraw

IN_IMAGE = "Budget Buddy.png"
OUT_IMAGE = "sample_out.png"
COORDS = (200, 200)
CIRCLE_SIZE = 100

def draw_on_img(in_img, out_path, x_coord, y_coord):
    image = Image.open(in_img)
    draw = ImageDraw.Draw(image)

    draw.ellipse((x_coord, y_coord, x_coord+CIRCLE_SIZE, y_coord+CIRCLE_SIZE),outline="red", fill=None, width=5)

    image.save(out_path)

if __name__ == "__main__":
    x_coord, y_coord = COORDS
    draw_on_img(IN_IMAGE, OUT_IMAGE, x_coord, y_coord)