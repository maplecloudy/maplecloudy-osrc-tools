// =================== DO NOT EDIT THIS FILE ====================
// Generated by Modello 1.9.1,
// any modifications will be overwritten.
// ==============================================================

package com.maplecloudy.osrc.model.maven;

/**
 * Class InputLocation.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings( "all" )
public final class InputLocation
    implements java.io.Serializable, InputLocationTracker
{

      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * The one-based line number. The value will be non-positive if
     * unknown.
     */
    private int lineNumber = -1;

    /**
     * The one-based column number. The value will be non-positive
     * if unknown.
     */
    private int columnNumber = -1;

    /**
     * Field source.
     */
    private InputSource source;

    /**
     * Field locations.
     */
    private java.util.Map<Object, InputLocation> locations;


      //----------------/
     //- Constructors -/
    //----------------/

    public InputLocation(int lineNumber, int columnNumber)
    {
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
    } //-- org.apache.maven.model.InputLocation(int, int)

    public InputLocation(int lineNumber, int columnNumber, InputSource source)
    {
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
        this.source = source;
    } //-- org.apache.maven.model.InputLocation(int, int, InputSource)


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method clone.
     *
     * @return InputLocation
     */
    public InputLocation clone()
    {
        try
        {
            InputLocation copy = (InputLocation) super.clone();

            if ( copy.locations != null )
            {
                copy.locations = new java.util.LinkedHashMap( copy.locations );
            }

            return copy;
        }
        catch ( Exception ex )
        {
            throw (RuntimeException) new UnsupportedOperationException( getClass().getName()
                + " does not support clone()" ).initCause( ex );
        }
    } //-- InputLocation clone()

    /**
     * Get the one-based column number. The value will be
     * non-positive if unknown.
     * 
     * @return int
     */
    public int getColumnNumber()
    {
        return this.columnNumber;
    } //-- int getColumnNumber()

    /**
     * Get the one-based line number. The value will be
     * non-positive if unknown.
     * 
     * @return int
     */
    public int getLineNumber()
    {
        return this.lineNumber;
    } //-- int getLineNumber()

    /**
     * 
     * 
     * @param key
     * @return InputLocation
     */
    public InputLocation getLocation( Object key )
    {
        return ( locations != null ) ? locations.get( key ) : null;
    } //-- InputLocation getLocation( Object )

    /**
     * 
     * 
     * @return Map
     */
    public java.util.Map<Object, InputLocation> getLocations()
    {
        return locations;
    } //-- java.util.Map<Object, InputLocation> getLocations()

    /**
     * Get the source field.
     * 
     * @return InputSource
     */
    public InputSource getSource()
    {
        return this.source;
    } //-- InputSource getSource()

    /**
     * Method merge.
     * 
     * @param target
     * @param sourceDominant
     * @param source
     * @return InputLocation
     */
    public static InputLocation merge( InputLocation target, InputLocation source, boolean sourceDominant )
    {
        if ( source == null )
        {
            return target;
        }
        else if ( target == null )
        {
            return source;
        }

        InputLocation result =
            new InputLocation( target.getLineNumber(), target.getColumnNumber(), target.getSource() );

        java.util.Map<Object, InputLocation> locations;
        java.util.Map<Object, InputLocation> sourceLocations = source.getLocations();
        java.util.Map<Object, InputLocation> targetLocations = target.getLocations();
        if ( sourceLocations == null )
        {
            locations = targetLocations;
        }
        else if ( targetLocations == null )
        {
            locations = sourceLocations;
        }
        else
        {
            locations = new java.util.LinkedHashMap();
            locations.putAll( sourceDominant ? targetLocations : sourceLocations );
            locations.putAll( sourceDominant ? sourceLocations : targetLocations );
        }
        result.setLocations( locations );

        return result;
    } //-- InputLocation merge( InputLocation, InputLocation, boolean )

    /**
     * Method merge.
     * 
     * @param target
     * @param indices
     * @param source
     * @return InputLocation
     */
    public static InputLocation merge( InputLocation target, InputLocation source, java.util.Collection<Integer> indices )
    {
        if ( source == null )
        {
            return target;
        }
        else if ( target == null )
        {
            return source;
        }

        InputLocation result =
            new InputLocation( target.getLineNumber(), target.getColumnNumber(), target.getSource() );

        java.util.Map<Object, InputLocation> locations;
        java.util.Map<Object, InputLocation> sourceLocations = source.getLocations();
        java.util.Map<Object, InputLocation> targetLocations = target.getLocations();
        if ( sourceLocations == null )
        {
            locations = targetLocations;
        }
        else if ( targetLocations == null )
        {
            locations = sourceLocations;
        }
        else
        {
            locations = new java.util.LinkedHashMap<Object, InputLocation>();
            for ( java.util.Iterator<Integer> it = indices.iterator(); it.hasNext(); )
            {
                InputLocation location;
                Integer index = it.next();
                if ( index.intValue() < 0 )
                {
                    location = sourceLocations.get( Integer.valueOf( ~index.intValue() ) );
                }
                else
                {
                    location = targetLocations.get( index );
                }
                locations.put( Integer.valueOf( locations.size() ), location );
            }
        }
        result.setLocations( locations );

        return result;
    } //-- InputLocation merge( InputLocation, InputLocation, java.util.Collection )

    /**
     * 
     * 
     * @param key
     * @param location
     */
    public void setLocation( Object key, InputLocation location )
    {
        if ( location != null )
        {
            if ( this.locations == null )
            {
                this.locations = new java.util.LinkedHashMap<Object, InputLocation>();
            }
            this.locations.put( key, location );
        }
    } //-- void setLocation( Object, InputLocation )

    /**
     * 
     * 
     * @param locations
     */
    public void setLocations( java.util.Map<Object, InputLocation> locations )
    {
        this.locations = locations;
    } //-- void setLocations( java.util.Map )

    
            

    @Override
    public String toString()
    {
        return getLineNumber() + " : " + getColumnNumber() + ", " + getSource();
    }
            
          
}