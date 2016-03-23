
************
set comment popups 
************
= the LabelTargetcontrolpanel (labeling from ontology) knows whenever a label is set (a call to setTarget...)
= whenever that happens, the targetcontrolpanel checks the label, sees if 'requestcomment' attribute on label is true, if so: fires a gui.util.SetCommentAction 
= the CSLConfig should be extended to define, as discussed, the name of the requestcomment attribute, which will be defined as an attribute on the layerinfo element.




************
Keystrokes for videolabeler
************
See also the mail exchange with jean!!!!!

When we want to do keystrokes for the keyboard shortcuts there are some hints for what should change, and a question.

The question first: *where do we define the keystrokes?* They are dependent on the ontology/objectset that you use. So. Is the keystroke-code an extra attribute to be included in the ontology xml file? Or is this a corpussetting? Remember, if you choose 'corpussetting', this means that for EVERY element in EVERY ontology there should be an entry in the nxtConfig.xml file. Duplicating ontologies is not good. So I would vote for defining 'default keystrokes' on the ontology, even if that's conceptually unsound. But you might disagree... PS: these 'default keystrokes' could also be used then for the named entity labeler.

And now for how to implement...
LabelTargetControlPanel is the class responsible for annotating from an ONTOLOGY. It will create buttons that fire something like a 'start new element of label X' event. These actions are created in "createButtons" in "LabelTargetControlPanel". If you collect these actions in an ActionMap, at the same time create an InputMap mapping from 'default keystroke' to the appropriate actions, and in the end make this inputmap/actionmap externally available, you can add this inputmap to all subwindows in the tool and then the keyboard shortcuts work.

DONT FORGET TO DOCUMENT

:-p

************
New controlpanel for videolabeler
************
I would like to have an extension to the Videolabeler: a targetControlPanel where I use one key (e.g. space) for segmentation only (so, for start/stop element events) and another list of keys (see "keystrokes for videolabeler") for assigning labels to the most recently created element. This is because when you are segmenting, your are probably faster if you can react as soon as you see SOMETHING occur, without first having to decide WHAT it was (i.e. what label you should assign to it).


************
Nesting Named Entities
************

in NECoder, line 157, the following method:
protected void createNewNE(NOMElement newType) {
    
is called to create a new named entity on the current selection in the transcription, given certain named entity type element from the ontology.
 Around line 170 the method checks if there is already a named entity defined.
 
 Around line 180 these old elements are removed.
 
 You can check here that, when removing the old named entities, only those are removed that are not either completely contained in the new NE or completely containing the new NE.

I would prefer you to make this a configurable setting in the nxtConfig.xml / NECoderConfig class. This means a new method NECoderConfig.allowNestedNamedEntities, and a related setting in nxtConfig.xml (include an explanation in the example settings!)
I guess this setting should be a corpussetting, not a gui setting.


************
some observation level resources needed
************

How does the annotator know which person (agent) belongs to which face in the video?
We might want to collect pictures of the people in a video, coupled to their agent shortname in the corpus, and show them on a panel on request...

************
'ctrl-rightclick' for replay
************

Some time ago we had a discussion with myrosia about the replay functionality connected with ctrl-right click, and how it should in the first place be optional and in the second place be possible to replace the 'ctrl-rightclick' with a certain keystroke (setReplayKeystroke() or something like that). I never got around to it. And... as soon as this is implemented it should be added to AbstractCallableToolConfig (as a setting which is defined in the nxtConfig guisettings section), initializing the NTranscriptionView in AbstractCallableTool.initilizeTranscriptionView using that value. MAC users might be happy with this possibility, their right mouse behaves strangely.



************
Clockface / video player
************

It would be nice if the clockface contained a dropdown list with which you can create a new player for one of the signals whenever you wish. Coupled with a 'close' button on the videoplayer (with cleanup and everything) it would be a nice addition to the flexibility of the system :o)

************
Recently opened file menu in nite.nxt.GUI?
************

relatively simple to build, makes a lot of people happy




************
Video labeler: default layer info's
************

now we need config data for EVERY layer that you want to annotate. It would be nice to be able to define in a dialog selected from the menu, that you want to label layer X with targetclasss Y etc. (so... menuitem 'new...', present user with list of layers, user selects layer, present user with list of relevant ontologies and objectsets for that layer, user selects one, ask user which targetcontrolpanel should be used...)