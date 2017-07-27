package com.xrun.user.util;

public class ErrorCode
{

	public static String SUCCESS = "0000";
	public static String ERROR = "-1000";

	public static String REQUEST_PARAMETER_IS_NULL = "2000";

	public static String UPDATE_USER_INFO_ERROR = "2001";
	public static String QUERY_USER_INFO_BY_ID_AND_TOKEN_ERROR = "2002";

	public static String QUERY_USER_PHOTO_BY_ID_AND_TOKEN_ERROR = "2003";

	public static String BAN_LOGIN = "2004";
	public static String INSERT_USER_SET_ERRO = "2005";

	public static String LOGIN_OUT_ERROR = "2006";

	public static String USER_THIRD_ACCOUNT_HAS_BINDED = "2007";

	public static String SELECT_USER_THIRD_ACCOUNT_IS_NULL_ERROR = "2008";

	public static String SELECT_USER_INFO_LIST_IS_NULL = "2009";

	public static String GET_REDIS_HASH_VALUE_ERROR = "2010";

	public static String THE_CURRENT_ACCOUNT_CANNOT_BE_UNBIND = "2011";
	public static String THE_ACCOUNT_HAS_BEEN_UNBIND = "2012";
	public static String THE_ACCOUNT_WAS_NOT_FOUND = "2013";

	public static String UPDATE_REDIS_ERROR = "2014";

	public static String QUERY_USER_PHOTOS_IS_NULL = "2015";

	public static String USER_NOT_REGISTER = "2016";

	public static String GET_SOCIAL_SERVICE_ERROR = "2017";
	public static String GET_USER_FRIEND_LIST_ERROR = "2018";
}
