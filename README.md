# Campaign-Writer

A JavaFX application to help you write and manage RPG Campaigns. The Application is extensible with plugins which
utilise the SDK artifact that exposes all interfaces required to build something awesome.

## Plugin Development
A Plugin needs to include the SDK artifact in their classpath to be able to utilize all public interfaces from the App.
A Plugin can be of different types, depending on which interfaces or abstract classes it implements. 

The base is always the Registrable Interface.

**ModulePlugin**. This is a bundle which will be used to load and save notes from a CampaignFile. It should use
some EditorPlugins otherwise it wont be able to edit anything.

**EditorPlugin**. This is the interface to manipulate a Note.

**ViewerPlugin**. This is the interface which allows a Note to be rendered, either inside or outside of the main window.

**DataPlugin**. A Data Plugin is placed inside the Data Menu and allows to manipulate the entire CampaignFile.

To allow a module to be registered use the `registryInterface` and its sole method `register` which is implemented via
`Registrable`.
