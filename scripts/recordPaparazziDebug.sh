#!/usr/bin/env bash

# Constants
SNAPSHOT_DIR="app/src/test/snapshots/images"
RESULT_PNG="result.png"

SCREENSHOT_FOLDER="app/src/test/snapshots/images/Pixel 8 Pro/ca.amandeep.nycairportsecuritylinewaits.ui_"
SCREENSHOT_1="${SCREENSHOT_FOLDER}MainScreenshotTest_screenshotMain[darkMode=true, device=Pixel 8 Pro, showNotifs=true].png"
SCREENSHOT_2_1="${SCREENSHOT_FOLDER}MainScreenshotTest_screenshotMain[darkMode=true, device=Pixel 8 Pro, showNotifs=true].png"
SCREENSHOT_2_2="${SCREENSHOT_FOLDER}MainScreenshotTest_screenshotMain[darkMode=false, device=Pixel 8 Pro, showNotifs=true].png"
SCREENSHOT_2="DiagonalDown.png"
SCREENSHOT_3="${SCREENSHOT_FOLDER}AirportScreenshotTest_screenshotMain[darkMode=true, device=Pixel 8 Pro, showNotifs=true, airport=EWR].png"
SCREENSHOT_4="${SCREENSHOT_FOLDER}AirportScreenshotTest_screenshotMain[darkMode=false, device=Pixel 8 Pro, showNotifs=true, airport=JFK].png"
SCREENSHOT_5="${SCREENSHOT_FOLDER}AirportScreenshotTest_screenshotMain[darkMode=true, device=Pixel 8 Pro, showNotifs=true, airport=LGA].png"
SCREENSHOT_6="${SCREENSHOT_FOLDER}AirportScreenshotTest_screenshotMain[darkMode=false, device=Pixel 8 Pro, showNotifs=true, airport=EWR].png"

FRAME_1="images/frame1.webp"
FRAME_2="images/frame2.webp"
FRAME_3="images/frame3.webp"
FRAME_4="images/frame4.webp"
FRAME_5="images/frame5.webp"
FRAME_6="images/frame6.webp"
FRAMED_1="images/framescr1.webp"
FRAMED_2="images/framescr2.webp"
FRAMED_3="images/framescr3.webp"
FRAMED_4="images/framescr4.webp"
FRAMED_5="images/framescr5.webp"
FRAMED_6="images/framescr6.webp"

# Function to check if a command exists
command_exists() {
    command -v "$1" &>/dev/null
}

# Verify all required commands are available
for cmd in mogrify convert python3; do
    if ! command_exists "$cmd"; then
        echo "Error: '$cmd' is not available. Please install it and try again."
        exit 1
    fi
done

echo "Preparing environment..."
rm -rf "$SNAPSHOT_DIR"

echo "Generating and organizing Paparazzi images..."
if ! ./gradlew recordPaparazziDebug; then
    echo "Error during 'recordPaparazziDebug'. Please check your setup."
    exit 1
fi

if ! python3 scripts/organize_paparazzi_images.py; then
    echo "Error during 'organize_paparazzi_images.py'. Please check the script."
    exit 1
fi

echo "Trimming images..."
for f in "$SCREENSHOT_2_1" "$SCREENSHOT_2_2" "$SCREENSHOT_3" "$SCREENSHOT_4" "$SCREENSHOT_5" "$SCREENSHOT_6"; do
    if ! mogrify -trim +repage "$f"; then
        echo "Error trimming $f. Please check the image file and mogrify command."
        exit 1
    fi
done

echo "Diagonalizing image..."
rm -f "$SCREENSHOT_2"
scripts/diagonalCombine.sh "$SCREENSHOT_2_1" "$SCREENSHOT_2_2"

echo "Framing images..."
for i in {1..6}; do
    FRAME_VAR="FRAME_$i"
    SCREENSHOT_VAR="SCREENSHOT_$i"
    FRAMED_VAR="FRAMED_$i"

    rm -f "$RESULT_PNG"
    if ! framer --oxipng-level 6 --pngquant-speed 1 "${!FRAME_VAR}" "${!SCREENSHOT_VAR}"; then
        echo "Error during image optimization. Please check 'framer' command."
        exit 1
    fi

    rm -f "${!FRAMED_VAR}"
    if ! convert -define webp:lossless=true -quality 100 "$RESULT_PNG" "${!FRAMED_VAR}"; then
        echo "Error converting $RESULT_PNG to WebP format. Please check the 'convert' command."
        exit 1
    fi
    rm -f "$RESULT_PNG"
    echo "Process completed successfully. Output file: $FRAMED_VAR"
    rm -f "$SCREENSHOT_VAR"
done

rm -f "$SCREENSHOT_2"

echo "Converting webp to png..."
for file in images/framescr*.webp; do
    rm -f "${file%.webp}.png"
    convert "$file" "${file%.webp}.png"
done
echo "Process completed successfully."
