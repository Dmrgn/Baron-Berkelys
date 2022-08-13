# ICS-2UP-ISP
### Baron Berkely's Best Bounty Hunting Hierachy for Magical Creatures and Contaminent
<h1>Summary</h1>
This was my final project for my grade 9 computer science class. It is a two dimensional top down rpg about a 'Death Eater' becoming a magical creature bounty hunter.

<h2>Level Creator</h2>
After coming up with the idea, the first thing I implemented was a simple level creator that loads JSON assets. You will see that the majority of the project is organized with JSON. This level creator can be used to load, edit and export levels. This significantly aided in level creation and if it weren't for my lack of time I would have invested more in this feature. 

<h2>Implementation</h2>
This project was created with Processing 3 and GLSL (Note: it is not compatible with Processing 4). The world is tile-based with entity sprites rendered per tile. The code was actually pretty organized until the final few days before submision when I turned some parts into prime spagetti code with extra cheese. I used GLSL to create a fragment shader that allows for real time raycasting to create lights and shadows. This was by far the most difficult piece to implement.

<h2>Issues</h2>
The Processing client does not seem to like GLSL shaders and so packaging the code as an executable results in a black screen.<br/><br/>
In addition, the very basis of the tile-entity system I set up is critcally flawed. Creating sprites per tile seems like a good idea as it allows for easy level editing. The issue is it is very hard to program logic for sprite position coordination between multiple classes when you are running on too few hours of sleep and have been staring at a screen for 8 hours trying to finish a project before its due. This results in several weird graphical issues where sprites in the background overlap forground elements.<br/><br/>
The most annoying issue I ran into was level creation - <strong>all</strong> 3 of the levels were created within 24 hours of the deadline. This lead to a broken victory condition being present at the end of the second level which makes getting to the third level impossible. Fortunately if you are on Processing 3 you can still access the level in all of its raycasting glory by going into the sandbox mode and loading the level (pressing 'l') "mushroomfull".

