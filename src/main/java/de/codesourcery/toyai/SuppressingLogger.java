package de.codesourcery.toyai;

import java.util.Objects;

public final class SuppressingLogger
{
	private String lastMessage;
	private int msgCount;
	private int suppressCount;

	public boolean isDebugEnabled() {
		return false;
	}

	public final void log(String message)
	{
		if ( Objects.equals( this.lastMessage , message ) )
		{
			msgCount++;
			if ( msgCount < 3 )
			{
				System.out.println( message );
			} else {
				suppressCount++;
			}
			return;
		}
		if ( suppressCount > 0 )
		{
			System.out.print( " [suppressed ");
			System.out.print( suppressCount );
			System.out.print( " times]: ");
			System.out.println( lastMessage );
			suppressCount = 0;
		}

		System.out.println( message );
		msgCount = 1;
		this.lastMessage = message;
	}
}
