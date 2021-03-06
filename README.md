# ZosShell  

Looking to add some speed to manipulate members and datasets? Are you a newbie to z/OS? Do you find the verbosity of ISPF or Zowe CLI time-consuming? Try this little app!  

This app provides a client Linux like shell to manipulate members and datasets on your z/OS backend.     
    
## Demo shows most of the common commands functionality:

![Demo](https://github.com/frankgiordano/ZosShell/blob/master/demos/main-demo.gif)

## Demo shows download (downloading member(s) from a z/OS instance to your local PC drive) functionality:

![Demo](https://github.com/frankgiordano/ZosShell/blob/master/demos/download-demo.gif)

## Demo shows vi (editing a member and saving it back to the z/OS instance) functionality:

![Demo](https://github.com/frankgiordano/ZosShell/blob/master/demos/save-demo.gif)
  
This project demonstrates the usage of [Zowe Client Java SDK](https://github.com/zowe/zowe-client-java-sdk).
  
Project provides a UI shell command prompt that allows you to manipulate datasets and its members. The shell performs the following linux like commands:  
  
    cat                     - display contents
    cd <arg>                - where arg is a dataset value or empty
    clear                   - clear the shell of all history
    h | help                - list commands
    history <arg>           - where arg is optional and indicates the number to display from bottom  
    hostname                - display current hostname connection
    !n                      - where n is a number, to execute command number n in history list   
    !string                 - will execute the last history command starting with that “string”
    !!                      - will execute the last history command
    ls <arg>                - where arg is optional and indicates a dataset or member value
                            - for member value only you can specified * wild card as last character
    ls -l <arg>             - where arg is optional and indicates a dataset or member value
                            - for member value only you can specified * wild card as last character
    ps                      - display all processes running
    ps <arg>                - where arg is a task/job name   
    pwd                     - show current working dataset
    rm <arg>                - where arg is "*", member, member with wildcard "*", dataset, or dataset with member value
    touch <arg>             - create member arg if it does not already exist
    uname                   - show current connected host name
    vi <arg>                - where arg is a sequential dataset or member name, arg will be downloaded 
                              and displayed for editing, use save command to save changes  
    whoami                  - show current connected user name
  
Along with following custom commands:  

    browsejob <arg1> <arg2>       - where arg1 is a job name and arg2 is optional
                                    if arg2 not specified, display job's JESMSGLG spool output
                                    if arg2 is equal to "all", display all job's spool output
    cancel <arg>                  - where arg is a task/job name  
    change <arg>                  - where arg is a number representing a connection
    clearlog                      - clear out the cached job log from last browsejob command 
    color <arg>                   - change color of prompt an text, arg i.e. blue, yellow, cyan etc..
    connections                   - a list of connection(s)   
    count members                 - return member count in current pwd dataset
    count datasets                - return dataset count in current pwd dataset
    cp | copy <arg> <arg>         - where arg can be ".", "*", member, dataset or dataset(member)
    download <arg1> <arg2>        - download arg1 to local c:\ZosShell\pwd where arg1 is member or sequential dataset  
                                    and arg2 is optional and only accepts "-b" for binary download      
    downloadjob <arg1> <arg2>     - download the latest job log where <arg1> is job name
                                    if arg2 not specified, download job's JESMSGLG spool output
                                    if arg2 is equal to "all", download all job's spool output
    mvs <arg>                     - execute a mvs command where arg is a command string within double quotes
    end                           - end session closes shell UI window
    files                         - list all files under local pwd drive value
    save <arg>                    - save arg where arg is a file name from files command to the current pwd
    search <arg>                  - search for arg within a job log from the last browsejob or tailjob command output  
    stop <arg>                    - where arg is a task/job name  
    submit <arg>                  - where arg is a member name  
    timeout <arg>                 - where arg is optional, with arg value you set new timeout, without shows current value
    tailjob <arg1> <arg2> <arg3>  - where arg1 is job name and arg2 and arg3 are optional
                                    use arg2 to specify either line limit or "all" value 
                                    if "all" is specified, display output from all of job's spool content
                                    line limit is 25 by default if not specified in arg2
    v | visited                   - a list of visited datasets  
  
The following key combinations provide the following functionality within the shell:  
  
    CTRL C                  - copy text
    CTRL V                  - paste coped text
    UP arrow                - scroll up through history list
    DOWN arrow              - scroll down through history list
    
To quit from the command shell UI, you can either press 'X' windows close icon or enter 'end' keyword.  
  
## Requirements  
  
    Java 11+ 
    Maven
    z/OSMF installed on your backend z/OS instance.
              
## Build And Execute  

Create a credentials.txt file under "C:\ZosShell" for Windows or "/ZosShell" on Max OSX directory that contains a list of z/OSMF connections per line with a comma delimiter for
connection values. You can change the drive\directory location by changing the hard coded value in the code.    
  
Format:  
    
    hostname,zomsfportnumber,username,password,mvsconsolename  
  
NOTE: "mvsconsolename" is optional and not needed. If you find trouble executing the mvs command, then your zosmf instance may be using a console name other than the default. If so, you can specify it here for the app to use when executing mvs command.   
        
At the root directory prompt, execute the following maven command:  
  
    mvn clean install  
  
Change directory to the target directory and execute the following command:  
  
    java -jar zosshell-1.0.jar   
  
If you are planning to browse large job output you may want to set the JVM memory usage higher than the default, i.e.  
  
    java -jar -Xmx2G zosshell-1.0.jar   
  
### Terminal color configuration (optional)
  
By default, the terminal will display its text in green on a black background. If you want to change those settings, follow the instructions bellow.  
  
Create a colors.txt file under "C:\ZosShell" for Windows or "/ZosShell" on Max OSX directory location that contains one line of two comma delimiter values to control the color scheme.    
  
First value will control the color of the text and prompt. Second value will control the color of the background panel.  
  
Format:  
  
    colornameornumbervalue,colornameornumbervalue  
  
Example:  
  
    449,white

![Demo](https://github.com/frankgiordano/ZosShell/blob/master/demos/color.gif)    

## Trouble Shooting
    
Logback logging is set up and configuration is located under src/main/resources/logback.xml  
    
It is configured to produce output logging while application is running under the running directory where the application was kicked off.
      
You are free to change configuration accordingly for your needs.  
    
  
  
