# material-colorpicker
Android view that lets you select material colors.  
  
How to use:
Import the colorpicker module in your project.

Inside settings.gradle:  
`include ':colorpicker'  `  
`project(':colorpicker').projectDir = file('path\\to\\colorpicker\\colorpicker')`  

Inside the apps build.gradle:  
`implementation project(':colorpicker')`  

xml styleables:  
`boxSize` - Set the size of the palette squares, will limit it to max height/width of screen.  
`boxGap` - Margin between boxes  
`boxStroke` - Width of stroke of selected box.  
  
` colorPickerView.addOnColorSelectedListener { selectedColor ->
            button.setBackgroundColor(selectedColor)
        }` - to listen to changes.  

Screenshots:
![Image One](https://github.com/elluid-data/material-colorpicker/blob/assets/colorpicker.png)  
![Image Two](https://github.com/elluid-data/material-colorpicker/blob/assets/colorpicker2.png)
