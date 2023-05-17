package com.whs.whsapi.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MapUtil {

	public static boolean areEqual( Map<String,Object> src, Map<String,Object> dest ) {
		if( src == null && dest == null )
			return true;
		if( src == null && dest != null )
			return false;
		Iterator<String> it = src.keySet().iterator();
		while( it.hasNext() ) {
			String key = it.next();
			if( src.get(key) != null && !src.get(key).equals(dest.get(key)) )
				return false;
		}
		return true;
	}
	
	public static List<String> listKeyString( Map<String, ?> map ){
		if( map == null )
			return null;
		return new ArrayList<String>(map.keySet());
	}
	
	public static Map<String,Object> load( String key, Object value ){
		Map<String,Object> ret = new LinkedHashMap<>();
		ret.put(key, value);
		return ret;
	}

	public static Map<String,Object> findByKey( List<Map<String,Object>> listMap, String key, Object value ){
		Map<String,Object> ret = null;
		if( listMap == null )
			return ret;
		for( int i = 0; i < listMap.size(); i++ ) {
			if( listMap.get(i).get(key) != null ) {
				if( listMap.get(i).get(key).equals(value) ) {
					return listMap.get(i);
				}
			}
		}
		return ret;
	}
	
	public static List<String> listKeys( List<Map<String,Object>> listMap ){
		List<String> ret = null;
		for( Map<String,Object> map : listMap ) {
			for( String key: map.keySet() ) {
				if( ret == null )
					ret = new ArrayList<>();
				ret.add(key);
			}
		}
		return ret;
	}
	private static void desensitizationMapSubProcessor(int level, Map<String, Object> map, String key, Object value) {
        if (value == null) {
            return;
        }
        if( !(value instanceof Map) && !(value instanceof List) ) {
            System.out.println("level: "+level+" key: "+key+" value: "+StringUtil.toString(value));
            return;
        } else if (value instanceof List) {
            List list = (List) value;
            for (Object object : list) {
                desensitizationMapSubProcessor(level+1, map, key, object);
            }
        } else if (value instanceof Map) {
            try {
                //noinspection unchecked
                Map<String, Object> subMap = (Map<String, Object>) value;
                //desensitizationMap(subMap);
                desensitizationMapSubProcessor(level+1, subMap, key, value);
            } catch (ClassCastException e) {

            }
        } else {
            throw new IllegalArgumentException(String.valueOf(value));
        }
    }
	
	public static void desensitizationMap( Map<String, Object> map ) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            desensitizationMapSubProcessor(0, map, key, value);
        }
    }
	public static String toString( Map<String, ?> map ){
		if( map == null )
			return null;
		List<String> listKey = listKeyString(map);
		StringBuilder sb = new StringBuilder();
		int idx = 0;
		for( String key: listKey ) {
			if( idx++ > 0 )
				sb.append(",");
			sb.append(key).append(":").append(map.get(key));
		}
		return sb.toString();
	}
	
	public static Map<String,Object> convertToConcurrent( Map<String,Object> map ){
		Map<String,Object> ret = null;
		if( map == null || map.isEmpty() )
			return ret;
		ret = new ConcurrentHashMap();
		for( String key: map.keySet() ) {
			ret.put(key, map.get(key));
		}
		return ret;
	}
	
	public static void removeNull( Map<String, ?> map ){
		if( map != null ) {
			for( String key: map.keySet() ) {
				Object obj = map.get(key);
				if( obj instanceof String ) {
					String valueOf = (String)obj;
					if( StringUtil.isNull(valueOf) )
						map.remove(key);
				}
			}
		}
	}
	
	public static void removeValue( List<Map<String,Object>> listMap, List<String> listKeyToRemove ) {
		for( int i = 0; listMap != null && i < listMap.size(); i++ ) {
			for( int j = 0; listKeyToRemove != null && j < listKeyToRemove.size(); j++ ) {
				listMap.get(i).remove(listKeyToRemove.get(j));	
			}
		}
	}
	
	public static void printContainer(Object cont, final int level, List<String> repo ) {
	    StringBuilder sb = new StringBuilder();
	    for (int i = 0; i < level; ++i) sb.append(" ");
	    if (cont instanceof List) {
	        List<Object> ls = (List<Object>)cont;
	        ls.forEach(obj -> {
	            if (obj == null)
	                repo.add(StringUtil.info("{}array v: null", sb));
	            else if (obj instanceof List || obj instanceof Map) {
	                printContainer(obj, level + 1, repo);
	            }
	            else if (obj instanceof String) {
	            	repo.add(StringUtil.info("{}v:{}", sb, obj));
	            }});
	    } else if (cont instanceof Map) {
	        Map<String, Object> mp = (Map<String, Object>)cont;
	        for (Map.Entry<String, Object> entry : mp.entrySet()) {
	            String key = entry.getKey();
	            Object value = entry.getValue();
	            if (value instanceof String) {
	            	repo.add(StringUtil.info("{}k:{} -> v:{}", sb, key, value));
	            } else if (value instanceof Map) {
	            	repo.add(StringUtil.info("{}map:{}", sb, key));
	                printContainer(value, level + 1, repo);
	            } else if (value instanceof List) {
	            	repo.add(StringUtil.info("{}array:{}", sb, key));
	                printContainer(value, level + 1, repo);
	            } else if (null == value) {
	            	repo.add(StringUtil.info("{}k:{} -> v:{}", sb, key, value));
	            } else {
	                throw new IllegalArgumentException(String.valueOf(value));
	            }
	        }
	    } else {
	        throw new IllegalArgumentException(String.valueOf(cont));
	    }
	}
	
	public static void getAllKeys(Map<String, Object> mapMain, List<String> keys) {
		mapMain.entrySet()
            .forEach(entry -> {
                keys.add(entry.getKey());
                if (entry.getValue() instanceof Map) {
                    Map<String, Object> map = (Map<String, Object>) entry.getValue();
                    getAllKeys(map, keys);
                } else if (entry.getValue() instanceof List) {
                    List<?> list = (List<?>) entry.getValue();
                    list.forEach(listEntry -> {
                        if (listEntry instanceof Map) {
                            Map<String, Object> map = (Map<String, Object>) listEntry;
                            getAllKeys(map, keys);
                        }
                    });
                }
            });
    }
	
	public static void getAllKeys(Map<String, Object> mapMain, List<String> keys, String parent) {
		mapMain.entrySet()
            .forEach(entry -> {
                keys.add((parent != null ? parent+";" : "")+entry.getKey());
                if (entry.getValue() instanceof Map) {
                    Map<String, Object> map = (Map<String, Object>) entry.getValue();
                    getAllKeys(map, keys, entry.getKey());
                } else if (entry.getValue() instanceof List) {
                    List<?> list = (List<?>) entry.getValue();
                    list.forEach(listEntry -> {
                        if (listEntry instanceof Map) {
                            Map<String, Object> map = (Map<String, Object>) listEntry;
                            getAllKeys(map, keys, entry.getKey());
                        }
                    });
                }
            });
    }

	public static void loadMapList(Map<String, Object> mapMain, Map<String, Object> found) {
		mapMain.entrySet().forEach(entry -> {
			if (entry.getValue() instanceof Map) {
				Map<String, Object> map = (Map<String, Object>) entry.getValue();
				if( found.containsKey(entry.getKey()) ) {
					if( found.get(entry.getKey()) instanceof Map ) {
						List<?> list = Arrays.asList(found.get(entry.getKey()),entry.getValue());
						found.put(entry.getKey(), list);
					} else if( found.get(entry.getKey()) instanceof List ) {
						List<Object> list = (List)found.get(entry.getKey());
						list.add(entry.getValue());
						found.put(entry.getKey(), list);
					}
					
				}else {
					found.put(entry.getKey(), entry.getValue());
				}
				loadMapList(map, found);
			} else if (entry.getValue() instanceof List) {
				List<?> list = (List<?>) entry.getValue();
				list.forEach(listEntry -> {
					if (listEntry instanceof Map) {
						Map<String, Object> map = (Map<String, Object>) listEntry;
						loadMapList(map, found);
					}
				});
			}
		});
    }

	public static void loadObject(Map<String, Object> mapMain, List<Object> found, String key) {
		mapMain.entrySet().forEach(entry -> {
			if (entry.getKey().equals(key))
				found.add(entry.getValue());
			if (entry.getValue() instanceof Map) {
				Map<String, Object> map = (Map<String, Object>) entry.getValue();
				loadObject(map, found, key);
			} else if (entry.getValue() instanceof List) {
				List<?> list = (List<?>) entry.getValue();
				list.forEach(listEntry -> {
					if (listEntry instanceof Map) {
						Map<String, Object> map = (Map<String, Object>) listEntry;
						loadObject(map, found, key);
					}
				});
			}
		});
    }

	public static List<Object> listObject( Map<String, Object> mapMain, String key ) {
		List<Object> listToFind = new ArrayList<>();
		loadObject(mapMain, listToFind, key);
		return listToFind;
	}

	public static Object getObject( Map<String, Object> mapMain, String key ) {
		Object ret = null;
		List<Object> listToFind = new ArrayList<>();
		loadObject(mapMain, listToFind, key);
		if( listToFind != null && listToFind.size() > 0 )
			ret = listToFind.get(0);
		return ret;
	}
	
	public static void main(String[] args) {
		String strJSON = "{\"source\":\"tbl_Construtora_Amaral\",\"destination\":\"builder\",\"pk\":[{\"CODIGO\":\"id\"}],\"column\":[{\"GRUPOCODIGO\":\"id_parent\"},{\"NOME\":\"name\"},{\"LOGO\":\"path_logo\"},{\"COMPLEMENTO\":\"complement\"}],\"exported\":{\"destination\":\"address\",\"column\":[{\"CEP\":\"zip_code\",\"concat(TIPO,' ',ENDERECO)\":\"street\",\"NUMERO\":\"number\",\"BAIRRO\":\"neighborhood\",\"CIDADE\":\"city\",\"UF\":\"state\"}],\"after\":{\"update\":{\"destination\":\"builder\",\"set\":[{\"id_address\":\"${address.id}\"}]}}}}";
		strJSON = "[{\"param\":\"param1\",\"val\":\"val1\"},{\"param\":\"param1\",\"val\":\"val1\"}]";
		Object obj = JSONUtil.readObject(strJSON);
		Map<String,Object> map = null;
		if( obj instanceof List ) {
			map = new LinkedHashMap<>();
			map.put("list", obj);
		}else {
			map = (Map)obj;
		}
		List<String> repo = new ArrayList<>();
		printContainer(map, 0, repo);
		for( String val: repo ) {
			System.out.println(val);
		}
		Map<String,Object> mapLevel = new LinkedHashMap<>();
		mapLevel.put("level-1", "level-1");
		Map<String,Object> mapLevel2 = new LinkedHashMap<>();
		mapLevel2.put("level-2", "level-2");
		Map<String,Object> mapLevel3 = new LinkedHashMap<>();
		mapLevel3.put("level-3", "level-3");
		mapLevel2.put("next-2", mapLevel3);
		mapLevel.put("next-1", mapLevel2);
		List<String> listKey = new ArrayList<>();
		getAllKeys(mapLevel, listKey, null);
		for( String key: listKey ) {
			System.out.println("k: "+key);
		}
		
		List<Object> found = new ArrayList<>();
		loadObject(mapLevel, found, "level-3");
		System.out.println(JSONUtil.toJSONString(found));
	}
}
