# Gym-Space-Monitoring-System

## Maven Set Up (Needed for all the Gemini API stuff)
In order to get the Gemini API pieces working I had to set up the project as a
Maven project; this requires a specific file structure which I already set up and
pushed to the repo. It also requires the use of a `pom.xml` file which I added.

If IntelliJ doesn't automatically recognize the `pom.xml` file as a Maven project, you can
right-click on the `pom.xml` file and select **"Add as Maven Project"**.
This will allow you to use the dependencies specified in the `pom.xml` file.

If IntelliJ still doesn't recognize the Maven project, you can try refreshing
the project by right-clicking on the project in the Project Explorer and
selecting **"Maven" > "Reload Project"**.

After that, just add packages to the `java` directory and add your code there.

## Gemini API Set Up
To use the Gemini API, you will need to set up an API key.

That can be done by going to: `https://aistudio.google.com/`

Once logged in, click on the **"Console"** tab in the top right corner. Then click on
**"API & Services"** in the left sidebar, and then click on **"Credentials"**.
From there, you can create a new API key by
clicking on the **"Create credentials"** button and selecting **"API key"**.

I have the code set up to read the API key from an environment variable called
`GOOGLE_API_KEY`. I'm the only Windows pleb so I'm not sure how to set it up on
Linux or Mac, but it's a pretty common thing so there should be good documentation
on Google.

***NOTE:*** The API key is not free, I thought it had a free amount but misread
it when looking at Google Cloud Console. However, it is pretty cheap after all the
testing and messing around I have spent a whole 50 cents. I just wanted to make sure
nobody is spending money they don't to. College is expensive enough. If anyone doesn't
want to deal with the API key but has some code they want tested with it, just 
let me know and I can run it for you. Just message me, and I'll get to it as soon as I can.

## Running the Code
In the `app` package, there are 3 different Java files that run the AI components
independently if you want to test them. This will require you to have the API key set up
and the Maven dependencies working.

`DemoAiDashboard` and `ShortAiTest` will require a webcam plugged in and enabled to work.

`ScannerGuiTest` will run without a webcam and without a scanner,
but without a scanner it's kind of pointless to run. So if
you want to borrow the scanner to mess with it just let me know.

## The Interesting AI Analysis Pieces
There's a lot of little moving pieces to get all the API calls working and parsed
correctly, but the most interesting pieces are in `aihazardanalyzer.gemini.GeminiVisionClient`.

There you can see the multiple API calls to Gemini. There is a separate call for each specific item I have it looking for.
Splitting the visual analysis into separate pipelines helped with both speed and
consistency since each call is focused on one job instead of trying to do
everything at once.

`soundmonitor.gemini.GeminiAudioClient` holds the call for the audio analysis.
You can see the context prompt and what I am asking it to do there.

`aihazardanalyzer.service.OccupancyAnalysisService`, `AggressionAnalysisService`,
`FallAnalysisService`, and `WalkwayAnalysisService` take the results from Gemini
and get them into the system to be utilized.

`aihazardanalyzer.service.OccupancyResultStabilizer`, `AggressionResultStabilizer`,
`FallResultStabilizer`, and `WalkwayResultStabilizer` stabilize the results from Gemini
over the last few frames to reduce flickering and false positives.

`aihazardanalyzer.capture.WebcamFrameCapture` does what the name says. It grabs the frame
for Gemini to analyze.

`soundmonitor.service.BulkyBuzzerService` handles the local sound threshold
that triggers the Bulky Buzzer alarm. If the alarm is tripped it triggers a call to Gemini
to analyze and classify the audio.

`app.DemoAiDashboard` is the main dashboard that displays all the results from Gemini.
It also has a webcam feed to show what Gemini is analyzing. It's not pretty, but it works.
That's why I leave the GUI work to the professionals.

The rest is really a lot of JSON parsing and helper pieces. Less exciting, but still necessary.